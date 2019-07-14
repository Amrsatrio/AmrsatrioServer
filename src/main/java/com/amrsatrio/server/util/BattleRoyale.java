package com.amrsatrio.server.util;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatMessageType;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumChatFormat;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.Packet;
import net.minecraft.server.v1_14_R1.PacketPlayOutChat;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BattleRoyale {
	private static final Logger LOGGER = LogManager.getLogger();
	//	private static final int[] WAITING_TIMES_SEC = new int[]{300, 200, 150, 120, 120, 90, 90, 60, 30};
//	private static final int[] SHRINKING_TIMES_SEC = new int[]{300, 140, 90, 60, 40, 30};
	private static final int[] WAITING_TIMES_SEC = new int[]{30};
	private static final int[] SHRINKING_TIMES_SEC = new int[]{15};
	private static final int FIRST_ZONE_DURATION_SEC = 30 * 20;//120 * 20;
	//	private final Random random = new Random();
	private final Plugin plugin;
	private World bukkitWorld;
	public BlockPosition regionPosStart;
	public BlockPosition regionPosEnd;
	public List<PlayArea> playAreas;
	//	public double playAreaSize;
	public int timeTicksIntoFirstArea;
	private long ticksElapsed;
	private PlayArea currentPlayArea;
	private int blueZoneStage;
	private int tickTask;
	private boolean running;
	private double factor = 0.6D;

	public BattleRoyale(Plugin plugin) {
		this.plugin = plugin;
	}

	public void setBukkitWorld(World bukkitWorld) {
		this.bukkitWorld = bukkitWorld;
	}

	public void start() {
		if (running) {
			throw new IllegalStateException("Already started");
		}

		int regionXSize = Math.abs(regionPosEnd.getX() - regionPosStart.getX());
		int regionZSize = Math.abs(regionPosEnd.getZ() - regionPosStart.getZ());
		int xLeft = Math.min(regionPosStart.getX(), regionPosEnd.getX());
		int zTop = Math.min(regionPosStart.getZ(), regionPosEnd.getZ());
		playAreas = new ArrayList<>();
		double playAreaSize = Math.max(regionXSize, regionZSize);
		ticksElapsed = 0;
		timeTicksIntoFirstArea = FIRST_ZONE_DURATION_SEC;
		blueZoneStage = -1;
		double cX = 0.0D;
		double cZ = 0.0D;
		int i = 0;
		while (playAreaSize >= 3.0D) {
			boolean first = i == 0;
//			double rangeX = first ? regionXSize : playAreaSize;
//			double rangeZ = first ? regionZSize : playAreaSize;
			// Set new play area size
			LOGGER.debug("play area size old " + playAreaSize + " i " + i);
			playAreaSize *= factor;
			LOGGER.debug("play area size new " + playAreaSize + " i " + i);
//			double halfPlayAreaSize = playAreaSize / 2.0D;
			if (first) {
//				cX = xLeft + halfPlayAreaSize + random.nextInt((int) (regionXSize - playAreaSize));
//				cZ = zTop + halfPlayAreaSize + random.nextInt((int) (regionZSize - playAreaSize));
				cX = xLeft + regionXSize / 2.0D;
				cZ = zTop + regionZSize / 2.0D;
			}
			setInitialWorldBorder(cX, cZ, xLeft, zTop, regionXSize, regionZSize);
			playAreas.add(new PlayArea(WAITING_TIMES_SEC[Math.min(i, WAITING_TIMES_SEC.length - 1)] * 20, SHRINKING_TIMES_SEC[Math.min(i, SHRINKING_TIMES_SEC.length - 1)] * 20, new XZd(cX, cZ), playAreaSize));
			i++;
		}
		LOGGER.debug("Total circles " + i);
		tickTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
			if (blueZoneStage == -1) {
				timeTicksIntoFirstArea--;
				if (timeTicksIntoFirstArea == 0) {
					doNextCircle();
				}
			} else if (currentPlayArea != null) {
				if (currentPlayArea.shrinking) {
					currentPlayArea.shrinkingTimeTicks--;
					if (currentPlayArea.shrinkingTimeTicks == 0) {
						if (blueZoneStage >= playAreas.size() - 1) {
							// End battle royale
							floatMsg(ChatColor.YELLOW + "Battle royale finished");
							stop();
						} else {
							doNextCircle();
						}
					}
				} else {
					currentPlayArea.timeTillLastCircleShrinkTicks--;
					if (currentPlayArea.timeTillLastCircleShrinkTicks == 0) {
						currentPlayArea.shrinking = true;
						currentPlayArea.alterWorldBorder(bukkitWorld, null);
						floatMsg(ChatColor.YELLOW + "Restricting play area!");
					}
				}
			}

			if (ticksElapsed % 20L == 0L) {
				IChatBaseComponent component;

				if (currentPlayArea != null && currentPlayArea.shrinking) {
					component = new ChatComponentText("!").a(ticksElapsed % 40L == 0L ? EnumChatFormat.WHITE : EnumChatFormat.RED);
				} else {
					component = new ChatComponentText(blueZoneStage == -1 || currentPlayArea == null ? "-" : millisToTime(currentPlayArea.timeTillLastCircleShrinkTicks * 50));
				}

				sendToEveryoneInWorld(Utils.headerFooterPacket(new ChatComponentText(""), new ChatComponentText("\n").addSibling(component)));
			}

			ticksElapsed++;
		}, 0L, 1L);
		running = true;
	}

	private void setInitialWorldBorder(double cX, double cZ, int xLeft, int zTop, int regionXSize, int regionZSize) {
		WorldBorder worldBorder = bukkitWorld.getWorldBorder();
		worldBorder.setCenter(cX, cZ);
		worldBorder.setSize(Collections.max(Lists.newArrayList(Math.abs(cX - xLeft), Math.abs(cZ - zTop), Math.abs(cX - xLeft + regionXSize), Math.abs(cZ - zTop + regionZSize))));
	}

	private void doNextCircle() {
//		PlayArea prev = null;
//		if (currentPlayArea != null) {
//			prev = currentPlayArea;
//		}
//		currentPlayArea = playAreas.get(++blueZoneStage);
//		if (prev != null) {
//			currentPlayArea.alterWorldBorder(bukkitWorld, prev);
//		}
		PlayArea prev = null;
		if (currentPlayArea != null) {
			prev = currentPlayArea;
		}
		PlayArea nextCircle1 = playAreas.get(++blueZoneStage);
		if (prev != null) {
			nextCircle1.alterWorldBorder(bukkitWorld, prev);
		}
		currentPlayArea = nextCircle1;
		LOGGER.debug("Current blueZoneStage " + blueZoneStage);
		floatMsg(ChatColor.YELLOW + "Proceed to play area marked on the map in " + millisToTime2(currentPlayArea.timeTillLastCircleShrinkTicks * 50) + "!");
	}

	private void floatMsg(String msg) {
		sendToEveryoneInWorld(new PacketPlayOutChat(new ChatComponentText(msg), ChatMessageType.GAME_INFO));
	}

	private void sendToEveryoneInWorld(Packet<?> packet) {
		for (EntityHuman eh : ((CraftWorld) bukkitWorld).getHandle().getPlayers()) {
			((EntityPlayer) eh).playerConnection.sendPacket(packet);
		}
	}

	public void stop() {
		if (!running) {
			return;
		}
		bukkitWorld.getWorldBorder().reset();
		plugin.getServer().getScheduler().cancelTask(tickTask);
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public World getAssignedWorld() {
		return bukkitWorld;
	}

	public int getBlueZoneStage() {
		return blueZoneStage;
	}

	public int getTotalBlueZoneStages() {
		return playAreas.size();
	}

	private static String millisToTime(long millis) {
		return DurationFormatUtils.formatDuration(millis, TimeUnit.MILLISECONDS.toMinutes(millis) < 1 ? "s" : "m:ss");
	}

	private static String millisToTime2(long millis) {
		return DurationFormatUtils.formatDurationWords(millis, true, true);
	}

	private static class PlayArea {
		public int timeTillLastCircleShrinkTicks;
		public int shrinkingTimeTicks;
		public boolean shrinking;
		public XZd center;
		public double size;

		public PlayArea(int timeTillLastCircleShrinkTicks, int shrinkingTimeTicks, XZd center, double size) {
			this.timeTillLastCircleShrinkTicks = timeTillLastCircleShrinkTicks;
			this.shrinkingTimeTicks = shrinkingTimeTicks;
			this.center = center;
			this.size = size;
		}

		public void alterWorldBorder(World world, PlayArea prev) {
			WorldBorder worldBorder = world.getWorldBorder();
			worldBorder.setCenter(center.x, center.z);
			if (shrinking) {
				worldBorder.setSize(size, shrinkingTimeTicks / 20);
			} else {
				// Make the world border idle (white circle position)
				worldBorder.setSize(prev.size);
			}
		}
	}

	private static class XZd {
		double x;
		double z;

		public XZd(double x, double z) {
			this.x = x;
			this.z = z;
		}
	}
}
