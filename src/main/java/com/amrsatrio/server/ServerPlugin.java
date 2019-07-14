package com.amrsatrio.server;

import com.amrsatrio.server.bukkitcompat.MCMABukkitCompat;
import com.amrsatrio.server.command.AbstractBrigadierCommand;
import com.amrsatrio.server.command.CommandBanExtended;
import com.amrsatrio.server.command.CommandBattleRoyale;
import com.amrsatrio.server.command.CommandBlockInformation;
import com.amrsatrio.server.command.CommandBuildText;
import com.amrsatrio.server.command.CommandConfig;
import com.amrsatrio.server.command.CommandDisplayLootTable;
import com.amrsatrio.server.command.CommandEditSign;
import com.amrsatrio.server.command.CommandEval;
import com.amrsatrio.server.command.CommandEvalSelector;
import com.amrsatrio.server.command.CommandExecutedCmdBlocks;
import com.amrsatrio.server.command.CommandGetBanner.GetBannerGui;
import com.amrsatrio.server.command.CommandGetExplorerMap;
import com.amrsatrio.server.command.CommandListFile;
import com.amrsatrio.server.command.CommandTpWorld;
import com.amrsatrio.server.command.CommandWelcomeTitle;
import com.amrsatrio.server.command.NonPlayerException;
import com.amrsatrio.server.command.WrongUsageException;
import com.amrsatrio.server.mapphone.PhoneMap;
import com.amrsatrio.server.mapphone.PhoneMap.PhoneInput;
import com.amrsatrio.server.util.Authenticator;
import com.amrsatrio.server.util.BattleRoyale;
import com.amrsatrio.server.util.ConfigManager;
import com.amrsatrio.server.util.Downloader;
import com.amrsatrio.server.util.Downloader.DownloadListener;
import com.amrsatrio.server.util.PingList;
import com.amrsatrio.server.util.PingList.PingEntry;
import com.amrsatrio.server.util.PropertiesEditor;
import com.amrsatrio.server.util.Title;
import com.amrsatrio.server.util.Utils;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.v1_14_R1.ChatClickable;
import net.minecraft.server.v1_14_R1.ChatClickable.EnumClickAction;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatHoverable;
import net.minecraft.server.v1_14_R1.ChatHoverable.EnumHoverAction;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.ChatModifier;
import net.minecraft.server.v1_14_R1.CommandDispatcher;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.EntityLiving;
import net.minecraft.server.v1_14_R1.EnumChatFormat;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_14_R1.command.CraftCommandMap;
import org.bukkit.craftbukkit.v1_14_R1.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftFallingBlock;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftThrownPotion;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftTippedArrow;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.projectiles.BlockProjectileSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.amrsatrio.server.util.Utils.actionBar;
import static com.amrsatrio.server.util.Utils.broke;
import static com.amrsatrio.server.util.Utils.buildString;
import static com.amrsatrio.server.util.Utils.clickBoxJson;
import static com.amrsatrio.server.util.Utils.extractZip;
import static com.amrsatrio.server.util.Utils.fancyTime;
import static com.amrsatrio.server.util.Utils.formatFileSize;
import static com.amrsatrio.server.util.Utils.getListener;
import static com.amrsatrio.server.util.Utils.getTimestampedPNGFileForDirectory;
import static com.amrsatrio.server.util.Utils.getTippedArrowItem;
import static com.amrsatrio.server.util.Utils.joinNiceString;
import static com.amrsatrio.server.util.Utils.ordinal;
import static com.amrsatrio.server.util.Utils.sha1;
import static com.amrsatrio.server.util.Utils.timeStringFromTicks;
import static com.amrsatrio.server.util.Utils.tripleBeepSamePitch;
import static com.amrsatrio.server.util.Utils.updateHF;
import static com.amrsatrio.server.util.Utils.zip;

public class ServerPlugin extends JavaPlugin implements Listener, PluginMessageListener {
	public static final Logger LOGGER = LogManager.getLogger(ServerPlugin.class.getSimpleName());
	public static final String SUPPORTED_NMS_VERSION = "1.14.3-R0.1-SNAPSHOT";
	public static final DecimalFormat HEALTH_DECIMAL_FORMAT = new DecimalFormat("#.##");
	public static final ChatColor SH_COLOR = ChatColor.BLUE;
	public static final ChatColor SH_MSG_COLOR = ChatColor.GRAY;
	public static final String SH = "%s> ";
	public static final String SH_WITH_COLORS = SH_COLOR + SH + SH_MSG_COLOR;
	//	public static final String FLY_SPEED_CONTAINER_TITLE = "Fly speed";
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	private static final String[] PW_GRADES = {"NA", "Unacceptable", "Very weak", "Poor", "Average", "Good", "Very Good", "Excellent"};
	private static final int MIN_PW_GRADE = 1;
	//	private static final List<UUID> PLUGIN_AUTHOR_UUIDS = Arrays.asList(UUID.fromString("77a3d6a0-49d5-45eb-bcb8-48dc26303c43"), UUID.fromString("beaa6a95-f065-4b75-883f-894488ec133e"));
	private static final List<String> PROTECTED_COMMANDS = Arrays.asList("password", "sconfig", "restart", "stop");
	//	private static final Gson PLAYER_HIST_ENTRY_JSON = new GsonBuilder().registerTypeAdapter(PlayerNameHistoryEntry.class, new PlayerNameHistoryEntry.Serializer()).create();
	public static Map<Player, PropertiesEditor> propEditInstances = new HashMap<>();
	public static Map<Player, Authenticator> pendingAuthPlayers = new HashMap<>();
	public static Map<Player, GetBannerGui> getBannerInstances = new HashMap<>();
	private static ServerPlugin instance;
	private final Map<Player, List<Damage>> playerDamages = new HashMap<>();
	private boolean backupRunning;
	private int clearingExecutedCmdBlocks;
	public List<BlockCommandSender> cmdBlockList = new ArrayList<>();
	public BattleRoyale battleRoyale;
	public ConfigManager configManager;
	public PhoneMap phoneMap;
	public PingList pingList;
	public ConfigManager.PluginConfig banTNT;
	public ConfigManager.PluginConfig bcPluginErrors;
	public ConfigManager.PluginConfig serverPingRecordsEnabled;
	public ConfigManager.PluginConfig mapGuiEnabled;
	public ConfigManager.PluginConfig showDamageSummary;

	@EventHandler
	public void a(AsyncPlayerChatEvent asyncplayerchatevent) {
		Player player = asyncplayerchatevent.getPlayer();

		if (pendingAuthPlayers.containsKey(player)) {
			pendingAuthPlayers.get(player).handleChat(asyncplayerchatevent);
			pendingAuthPlayers.remove(player);
			asyncplayerchatevent.setCancelled(true);
			return;
		} else if (propEditInstances.containsKey(player)) {
			propEditInstances.get(player).handleChat(asyncplayerchatevent);
			return;
		}

		asyncplayerchatevent.setMessage(ChatColor.translateAlternateColorCodes('&', asyncplayerchatevent.getMessage()));
	}

	@EventHandler
	public void a(EntityDamageByEntityEvent entitydamagebyentityevent) {
		if (!showDamageSummary.get() || entitydamagebyentityevent.getDamager().getType() != EntityType.PLAYER && !(entitydamagebyentityevent.getDamager() instanceof Projectile)) {
			return;
		}

		try {
			Entity entity = entitydamagebyentityevent.getDamager() instanceof Projectile && ((Projectile) entitydamagebyentityevent.getDamager()).getShooter() != null && ((Projectile) entitydamagebyentityevent.getDamager()).getShooter() instanceof Entity ? (Entity) ((Projectile) entitydamagebyentityevent.getDamager()).getShooter() : entitydamagebyentityevent.getDamager();

			if (!(entity instanceof Player)) {
				return;
			}

			IChatBaseComponent ichatbasecomponent = new ChatComponentText("\u00a7b<-- ").addSibling(damageSummary(new Damage(entitydamagebyentityevent), true)).a(" \u00a7b-->");
			actionBar((Player) entity, ichatbasecomponent);
		} catch (Throwable throwable) {
			broke(throwable);
		}
	}

	@EventHandler
	public void a(SignChangeEvent signchangeevent) {
		String[] lines = signchangeevent.getLines();

		for (int i = 0; i < lines.length; i++) {
			signchangeevent.setLine(i, ChatColor.translateAlternateColorCodes('&', lines[i]));
		}
	}

	@EventHandler
	public void a(EntityExplodeEvent entityexplodeevent) {
		if (banTNT.get() && entityexplodeevent.getEntityType() == EntityType.PRIMED_TNT) {
			int i = 0;
			entityexplodeevent.setCancelled(true);

			for (Entity entity : entityexplodeevent.getEntity().getWorld().getEntitiesByClass(TNTPrimed.class)) {
				if (entity != entityexplodeevent.getEntity()) {
					entity.remove();
					i++;
				}
			}

			StringBuilder stringbuilder = new StringBuilder(ChatColor.GREEN + String.format(Messages.BanTnt.PREVENTED_BLOCKS, entityexplodeevent.blockList().size()));

			if (i > 0) {
				stringbuilder.append(String.format(Messages.BanTnt.REMOVED_TNTS, i));
			}

			for (Player player : entityexplodeevent.getEntity().getWorld().getPlayers()) {
				actionBar(player, stringbuilder.toString());
			}
		}
	}

	@EventHandler
	public void a(InventoryClickEvent inventoryclickevent) {
		try {
			Player player = (Player) inventoryclickevent.getWhoClicked();
			// TODO fly speed GUI disabled, find out a way to get inventory title
//			Inventory inventory = inventoryclickevent.getInventory();
//
//			if (inventory.getName().equals(FLY_SPEED_CONTAINER_TITLE)) {
//				inventoryclickevent.setCancelled(true);
//				if (inventoryclickevent.getSlot() == 10) {
//					if (!(player.getFlySpeed() == 0.05f)) {
//						//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.4f);
//						player.setFlySpeed((player.getFlySpeed() * 10 - 0.05f * 10) / 10);
//					} else {
//						player.playSound(player.getLocation(), "minecraft:entity.item.break", 3.0f, 0.5f);
//					}
//				}
//
//				if (inventoryclickevent.getSlot() == 11) {
//					//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.0f);
//					player.setFlySpeed(0.1f);
//				}
//
//				if (inventoryclickevent.getSlot() == 12) {
//					if (!(player.getFlySpeed() == 1.0f)) {
//						//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.4f);
//						player.setFlySpeed((player.getFlySpeed() * 10 + 0.05f * 10) / 10);
//					} else {
//						player.playSound(player.getLocation(), "minecraft:entity.item.break", 3.0f, 0.5f);
//					}
//				}
//
//				ItemStack itemstack = inventoryclickevent.getInventory().getItem(11);
//				ItemMeta itemmeta = itemstack.getItemMeta();
//				itemmeta.setDisplayName("Your current fly speed: " + player.getFlySpeed());
//				itemstack.setItemMeta(itemmeta);
//				inventoryclickevent.getInventory().setItem(11, itemstack);
//			}

			if (propEditInstances.containsKey(player)) {
				propEditInstances.get(player).a(inventoryclickevent);
			}

			if (CommandListFile.fileGuis.containsKey(player)) {
				CommandListFile.fileGuis.get(player).handle(inventoryclickevent);
			}

			if (getBannerInstances.containsKey(player)) {
				getBannerInstances.get(player).handle(inventoryclickevent);
			}
		} catch (Throwable e) {
			broke(e);
		}
	}

	@EventHandler
	public void a(InventoryCloseEvent inventorycloseevent) {
		Player player = (Player) inventorycloseevent.getPlayer();

		if (CommandListFile.fileGuis.containsKey(player)) {
			CommandListFile.fileGuis.get(player).handleClose(inventorycloseevent);
		}

		if (propEditInstances.containsKey(player) && !propEditInstances.get(player).waiting) {
			propEditInstances.remove(player);
		}

		if (getBannerInstances.containsKey(player) && !getBannerInstances.get(player).switching) {
			getBannerInstances.remove(player);
		}
	}

//	public synchronized boolean a(net.minecraft.server.v1_14_R1.World world, Random random, ChunkCoordIntPair chunkcoordintpair, StructureStart structurestart) {
//		int i = (chunkcoordintpair.x << 4) + 8;
//		int j = (chunkcoordintpair.z << 4) + 8;
//		boolean flag = false;
//		if (structurestart.a() && structurestart.a(chunkcoordintpair) && structurestart.b().a(i, j, i + 15, j + 15)) {
//			structurestart.a(world, random, new StructureBoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
//			structurestart.b(chunkcoordintpair);
//			flag = true;
//		}
//		return flag;
//	}

	@EventHandler
	public void a(PlayerChangedWorldEvent playerchangedworldevent) {
		World world = playerchangedworldevent.getFrom();
		List<World> list = getServer().getWorlds();

		if (world.getPlayers().size() == 0 && world != list.get(0) && world != list.get(1) && world != list.get(2)) {
			getServer().unloadWorld(world.getName(), true);
			getServer().getWorlds().remove(world);
		}

		updateHF(playerchangedworldevent.getPlayer());
	}

	@EventHandler
	public void a(final PlayerCommandPreprocessEvent playercommandpreprocessevent) {
		final String s = playercommandpreprocessevent.getMessage().substring(1);
		String[] astring = s.split(" ", 2);
		final String label = astring[0];
		deprecate(playercommandpreprocessevent.getPlayer(), label, "text", "buildtext");

		if (PROTECTED_COMMANDS.contains(label)) {
			playercommandpreprocessevent.setCancelled(true);
			makeAnAuth(playercommandpreprocessevent.getPlayer(), astring, () -> getServer().dispatchCommand(playercommandpreprocessevent.getPlayer(), s));
		}
	}

	@EventHandler
	public void a(PlayerDeathEvent playerdeathevent) {
		try {
			if (!showDamageSummary.get() || !playerDamages.containsKey(playerdeathevent.getEntity())) {
				return;
			}

			playerdeathevent.getEntity().sendMessage(ChatColor.DARK_GREEN + "--- " + Messages.DamageSummary.TITLE + " (" + timeStringFromTicks(playerdeathevent.getEntity().getStatistic(Statistic.TIME_SINCE_DEATH)) + ") ---");
			int i = 1;

			for (Damage damage : playerDamages.get(playerdeathevent.getEntity())) {
				((CraftPlayer) playerdeathevent.getEntity()).getHandle().sendMessage(new ChatComponentText("\u00a7b" + i++ + ". ").addSibling(damageSummary(damage)));
			}

			playerDamages.remove(playerdeathevent.getEntity());
		} catch (Throwable e) {
			broke(e);
		}
	}

	@EventHandler
	public void a(PlayerJoinEvent playerjoinevent) {
		final Player player = playerjoinevent.getPlayer();

		updateHF(player);

		if (getConfig().getBoolean("welcome-state")) {
			welcomeTitle(player);
		}

		if (pingList != null) {
			pingList.addEntry(player);
		}
	}

//	@EventHandler
//	public void a(AsyncPlayerPreLoginEvent asyncplayerpreloginevent) {
//		Result result = asyncplayerpreloginevent.getLoginResult();
//		String s = asyncplayerpreloginevent.getName();
//		getServer().broadcastMessage(ChatColor.GRAY + (result != Result.ALLOWED ? String.format("Unfortunately, %s is refused to log in because %s", s, getRefuseMessage(result)) : s + " is logging in..."));
//	}

	@EventHandler
	public void a(PlayerQuitEvent a) {
		Player player = a.getPlayer();
		pendingAuthPlayers.remove(player);
		propEditInstances.remove(player);
		CommandListFile.fileGuis.remove(player);
		getBannerInstances.remove(player);
	}

	@EventHandler
	public void a(ServerListPingEvent serverlistpingevent) {
		try {
			if (pingList != null) {
				pingList.addEntry(serverlistpingevent);
				IChatBaseComponent ichatbasecomponent = new ChatComponentText("");
				PingEntry pingentry = pingList.get(serverlistpingevent.getAddress());
				IChatBaseComponent players = getPingedPlayerNames(pingentry);

				if (players != null) {
					ichatbasecomponent.addSibling(players);
				}

				InetAddress inetaddress = serverlistpingevent.getAddress();
				String s = inetaddress.getHostAddress();
				ichatbasecomponent.addSibling(new ChatMessage("%s%s pinged the server for the %s time today", players == null ? "" : "@", new ChatComponentText(s).setChatModifier(new ChatModifier().setChatClickable(new ChatClickable(EnumClickAction.OPEN_URL, "http://whatismyipaddress.com/ip/" + s)).setChatHoverable(new ChatHoverable(EnumHoverAction.SHOW_TEXT, new ChatComponentText("Click for more info on this IP address"))).setUnderline(true)), ordinal(pingentry.getHowManyTimesPingedToday())).setChatModifier(new ChatModifier().setColor(EnumChatFormat.GRAY)));
				((CraftServer) getServer()).getHandle().sendMessage(ichatbasecomponent);
			}
		} catch (Throwable e) {
			LOGGER.warn("Unexpected error occurred in ping event", e);
		}
	}

	@EventHandler
	public void a(EntityDamageEvent entitydamageevent) {
		if (!showDamageSummary.get() || entitydamageevent.getEntityType() != EntityType.PLAYER) {
			return;
		}

		Player entity = (Player) entitydamageevent.getEntity();
		List<Damage> list = playerDamages.containsKey(entity) ? playerDamages.get(entity) : new ArrayList<>();
		Damage damage = new Damage(entitydamageevent);
		list.add(damage);
		playerDamages.put(entity, list);

		try {
			Utils.actionBar((Player) entitydamageevent.getEntity(), damageSummary(damage));
		} catch (Exception e) {
			broke(e);
		}
	}

	@EventHandler
	public void a(WorldLoadEvent worldloadevent) {
		getServer().broadcastMessage(ChatColor.GRAY + "World " + ChatColor.BOLD + worldloadevent.getWorld().getName() + ChatColor.RESET + ChatColor.GRAY + " is being loaded. Expect lag for a while.");
	}

	@EventHandler
	public void a(WorldUnloadEvent worldunloadevent) {
		getServer().broadcastMessage(ChatColor.GRAY + "World " + ChatColor.BOLD + worldunloadevent.getWorld().getName() + ChatColor.RESET + ChatColor.GRAY + " is being unloaded.");
	}

	@EventHandler
	public void a(ServerCommandEvent servercommandevent) {
		CommandSender sender = servercommandevent.getSender();

		if (sender instanceof BlockCommandSender) {
			cmdBlockList.add((BlockCommandSender) sender);
		}
	}

	private IChatBaseComponent getPingedPlayerNames(PingEntry pingentry) {
		if (pingentry == null || pingentry.names.isEmpty()) {
			return null;
		}

		String[] astring = new String[pingentry.names.size()];
		Arrays.fill(astring, "%s");
		return new ChatMessage(joinNiceString(astring, "or"), pingentry.names.stream().map(gameprofile -> new ChatComponentText(gameprofile.getName()).setChatModifier(new ChatModifier().setChatHoverable(new ChatHoverable(EnumHoverAction.SHOW_TEXT, new ChatComponentText(gameprofile.getName() + "\n" + ChatColor.GRAY + gameprofile.getId()))).setChatClickable(new ChatClickable(EnumClickAction.SUGGEST_COMMAND, gameprofile.getId().toString())))).toArray());
	}

	private void changePassword(CommandSender commandsender, String[] astring) throws NoSuchAlgorithmException {
		if (astring[0].equals("clear")) {
			if (getConfig().getString("password").isEmpty()) {
				msg(commandsender, Messages.Authenticator.NOT_YET_SET);
				return;
			}

			getConfig().set("password", "");
			saveConfig();
			msg(commandsender, ChatColor.GREEN + Messages.Authenticator.CLEARED_PASSWORD);
		} else {
			if (!astring[0].equals(astring[1])) {
				msg(commandsender, ChatColor.RED + Messages.Authenticator.NOT_MATCH);
				tripleBeepSamePitch(commandsender, this);
				return;
			}

			int i = Authenticator.getPasswordStrength(astring[0]);
			String pwStrength = String.format(Messages.Authenticator.STRENGTH, PW_GRADES[i]);

			if (i < MIN_PW_GRADE) {
				msg(commandsender, ChatColor.RED + Messages.Authenticator.TOO_WEAK, pwStrength);
				tripleBeepSamePitch(commandsender, this);
				return;
			}

			getConfig().set("password", sha1(astring[0]));
			saveConfig();
			msg(commandsender, ChatColor.GREEN + Messages.Authenticator.CHANGED_PASSWORD + ' ' + pwStrength);
		}
	}

	private IChatBaseComponent damageSummary(Damage damage, boolean fromMe) throws Exception {
		EntityDamageEvent entitydamageevent = damage.event;
		// Damage title
		IChatBaseComponent icbc = new ChatComponentText(String.format("\u00a76%s:\u00a7r", StringUtils.capitalize(entitydamageevent.getCause().toString().toLowerCase().replaceAll("_", " "))));

		if (entitydamageevent instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) entitydamageevent;
			Entity damager = e.getDamager();
			String fromTo = "\u00a77" + (fromMe ? "to" : "from") + ChatColor.RESET;
			boolean showFromToObject = true;
			//Entity fromto2 = fromMe ? e.getEntity() : damager;
			//System.out.println(fromto2);

			if (entitydamageevent.getCause() == DamageCause.PROJECTILE && damager instanceof Projectile && ((Projectile) damager).getShooter() != null) {
				Projectile projectile = (Projectile) damager;
				icbc.addSibling(new ChatMessage(" %s%s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName(), damager instanceof CraftTippedArrow ? new ChatComponentText(" ").addSibling(getTippedArrowItem((CraftTippedArrow) damager).B()) : ""));
				//				if (damager instanceof TippedArrow)
				//					System.out.println(((CraftTippedArrow) damager).getHandle().effects);

				if (projectile.getShooter() instanceof Entity) {
					damager = (Entity) projectile.getShooter();
				} else if (projectile.getShooter() instanceof BlockProjectileSource) {
					icbc.addSibling(new ChatMessage(" \u00a77from a\u00a7r %s", new ChatMessage(((CraftBlock) ((BlockProjectileSource) projectile.getShooter()).getBlock()).getNMS().getBlock().l())));
					showFromToObject = false;
				} else {
					showFromToObject = false;
				}
			}

			if (entitydamageevent.getCause() == DamageCause.MAGIC) {
				if (damager instanceof ThrownPotion) {
					icbc.addSibling(new ChatMessage(" %s %s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName(), ((CraftThrownPotion) damager).getHandle().getItem().B()));
					showFromToObject = false;

					if (((Projectile) damager).getShooter() != null) {
						if (((Projectile) damager).getShooter() instanceof Entity) {
							damager = (Entity) ((Projectile) damager).getShooter();
							showFromToObject = true;
						}
					}
				}
			}

			if (damager instanceof AreaEffectCloud) {
				if (((AreaEffectCloud) damager).getSource() != null) {
					icbc.addSibling(new ChatMessage(" %s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName()));
					damager = (Entity) ((AreaEffectCloud) damager).getSource();
				}
			}

			Entity fromto1 = fromMe ? e.getEntity() : damager;

			if (showFromToObject) {
				icbc.addSibling(new ChatMessage(" %s %s", fromTo, damager == e.getEntity() ? "yourself" : ((CraftEntity) fromto1).getHandle().getScoreboardDisplayName()));
			}

			if (entitydamageevent.getCause() == DamageCause.FALLING_BLOCK && damager instanceof FallingBlock) {
				icbc.addSibling(new ChatMessage(" (%s)", new ChatMessage(((CraftFallingBlock) damager).getHandle().getBlock().getBlock().l())));
			}

			if (damager instanceof LivingEntity) {
				EntityLiving el = ((CraftLivingEntity) damager).getHandle();
				net.minecraft.server.v1_14_R1.ItemStack usingItem = el.getItemInMainHand();

				if (!usingItem.isEmpty()) {
					icbc.addSibling(new ChatMessage(" \u00a77using\u00a7r %s", usingItem.B()));
				}
			}
		}

		// Health reduction count
		icbc.a(" \u00a7c-" + HEALTH_DECIMAL_FORMAT.format(entitydamageevent.getFinalDamage()));
		long l = System.currentTimeMillis() - damage.timestamp;

		if (l >= 1000) {
			icbc.a(String.format(" \u00a79%ss", HEALTH_DECIMAL_FORMAT.format((double) l / 1000d)));
		}

		return icbc;
	}

	/*private IChatBaseComponent damageSummary0(Damage damage, boolean didBySelf) throws Exception {
		// YOU died from falling
		// YOU were hit by falling
		EntityDamageEvent event = damage.event;
		// Initialize the root component: damage cause as title
		// ex: "Projectile: "
		IChatBaseComponent subject; // this is damager
		IChatBaseComponent root = new ChatComponentText(String.format("%s:", StringUtils.capitalize(event.getCause().toString().toLowerCase().replaceAll("_", " "))));

		if (event instanceof EntityDamageByEntityEvent) {
			// YOU <hit|killed> Skeleton with [Diamond Sword]
			// Skeleton <hit|killed> YOU with [Bow]
			// YOU <hit|killed> YOURSELF with TNT
			// Alex <hit|killed> YOU with Tipped Arrow [Potion of Blindness]
			// YOU <hit|killed> YOURSELF with Arrow
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
			CombatTracker combattracker = ((CraftLivingEntity) e.getEntity()).getHandle().getCombatTracker();
			IChatBaseComponent verb, complement;
			Entity damager = e.getDamager();
			boolean showFromToCmpnt = true;
			//Entity fromto2 = didBySelf ? e.getEntity() : damager;
			//System.out.println(fromto2);

			// Damaged by projectile
			if (event.getCause() == DamageCause.PROJECTILE && damager instanceof Projectile && ((Projectile) damager).getShooter() != null) {
				Projectile projectile = (Projectile) damager;
				root.addSibling(new ChatMessage(" %s%s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName(), damager instanceof TippedArrow ? new ChatComponentText(" ").addSibling(getTippedArrowItem((TippedArrow) damager).A()) : ""));
//				if (damager instanceof TippedArrow)
//					System.out.println(((CraftTippedArrow) damager).getHandle().effects);

				if (projectile.getShooter() instanceof Entity) {
					damager = (Entity) projectile.getShooter();
				} else if (projectile.getShooter() instanceof BlockProjectileSource) {
					root.a(" from a " + ((BlockProjectileSource) projectile.getShooter()).getBlock().getType().toString().toLowerCase().replaceAll("_", " "));
					showFromToCmpnt = false;
				} else {
					showFromToCmpnt = false;
				}
			}

			if (event.getCause() == DamageCause.MAGIC) {
				if (damager instanceof ThrownPotion) {
					// ex: " <thrownPotionDisplayName> <potionItemName>"
					root.addSibling(new ChatMessage(" %s %s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName(), ((CraftThrownPotion) damager).getHandle().getItem().A()));
					showFromToCmpnt = false;
					Projectile projectile = (Projectile) damager;

					if (projectile.getShooter() != null) {
						if (projectile.getShooter() instanceof Entity) {
							damager = (Entity) projectile.getShooter();
							showFromToCmpnt = true;
						}
					}
				}
			}

			if (damager instanceof AreaEffectCloud) {
				if (((AreaEffectCloud) damager).getSource() != null) {
					root.addSibling(new ChatMessage(" %s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName()));
					damager = (Entity) ((AreaEffectCloud) damager).getSource();
				}
			}

			Entity damagee = e.getEntity();
			Entity fromto1 = didBySelf ? damagee : damager;

			if (showFromToCmpnt) {
				// ex when hit by self: " <from|to> <yourself|itself>"
				// ex when hit other entity: " <from|to> <entityDisplayName>"
				String direction = didBySelf ? "to" : "from";
				root.addSibling(new ChatMessage(" %s %s", direction, damager == damagee ? didBySelf ? "yourself" : "itself" : ((CraftEntity) fromto1).getHandle().getScoreboardDisplayName()));
			}

			if (event.getCause() == DamageCause.FALLING_BLOCK && damager instanceof FallingBlock) {
				root.addSibling(new ChatMessage(" (%s)", ((CraftFallingBlock) damager).getHandle().getBlock().getBlock().m()));
			}

			// Add the item used by the damager to damage the target
			if (damager instanceof LivingEntity) {
				EntityLiving el = ((CraftLivingEntity) damager).getHandle();
				net.minecraft.server.v1_14_R1.ItemStack usingItem = el.getItemInMainHand();

				if (!usingItem.isEmpty()) {
					root.addSibling(new ChatMessage(" using %s", usingItem.A()));
				}
			}
		} else if (event instanceof EntityDamageByBlockEvent) {
			Block damagerBlock = ((EntityDamageByBlockEvent) event).getDamager();
		}

		// Health reduction count
		// ex: " -2.00"
		root.a(" \u00a7c-" + HEALTH_DECIMAL_FORMAT.format(event.getFinalDamage()));
		long l = System.currentTimeMillis() - damage.timestamp;

		if (l >= 1000) {
			// Time since the damage occurred
			// ex: " 7s"
			root.a(String.format(" \u00a79%ss", HEALTH_DECIMAL_FORMAT.format((double) l / 1000d)));
		}

		return root;
	}*/

	private IChatBaseComponent damageSummary(Damage damage) throws Exception {
		return damageSummary(damage, false);
	}

	/*private boolean genStructure(StructureGenerator a, BlockPosition b, Chunk cc, int d) throws Exception {
		Random r = new Random();
		net.minecraft.server.v1_14_R1.World w2 = ((CraftWorld) cc.getWorld()).getHandle();
//		StructureStart structurestart = null;
//		if (a instanceof WorldGenVillage)
//			structurestart = new WorldGenVillage.WorldGenVillageStart(w2, r, cc.getX(), cc.getZ(), d);
//		else if (a instanceof WorldGenLargeFeature)
//			structurestart = new WorldGenLargeFeature.WorldGenLargeFeatureStart(w2, r, cc.getX(), cc.getZ());
//		else if (a instanceof WorldGenNether)
//			structurestart = new WorldGenNether.WorldGenNetherStart(w2, r, cc.getX(), cc.getZ());
//		else if (a instanceof WorldGenStronghold)
//			structurestart = new WorldGenStronghold.WorldGenStronghold2Start(w2, r, cc.getX(), cc.getZ());
//		else if (a instanceof WorldGenEndCity) {
//			ChunkProviderTheEnd cpe = new ChunkProviderTheEnd(w2, true, r.nextLong());
//			structurestart = new WorldGenEndCity.Start(w2, cpe, r, cc.getX(), cc.getZ());
//		} else structurestart = new WorldGenMonument.WorldGenMonumentStart(w2, r, cc.getX(), cc.getZ());
		Method method = a.getClass().getDeclaredMethod("b", int.class, int.class);
		method.setAccessible(true);
		return a(w2, new Random(), new ChunkCoordIntPair(b), (StructureStart) method.invoke(a, cc.getX(), cc.getZ()));
	}*/

	/*public static PlayerNameHistoryEntry[] getPNameHistory(UUID a) throws IOException {
		String s = readUrl("https://api.mojang.com/user/profiles/" + a.toString().replaceAll("-", "") + "/names");

		if (s.length() == 0) {
			throw new RuntimeException("The server responded nothing. Try deleting the user cache.");
		}

		return PLAYER_HIST_ENTRY_JSON.fromJson(s, PlayerNameHistoryEntry[].class);
	}

	public static void listPNameHistory(final CommandSender commandsender, final String s) {
		new Thread(() -> {
			try {
				if (!getServer().getOnlineMode()) {
					throw new CommandException(Messages.PlayerHist.OFF_MODE);
				}

				msg(commandsender, String.format(Messages.PlayerHist.HEADER, s));
				GameProfile gameprofile = ((CraftServer) getServer().getServer()).getHandle().getServer().getUserCache().getProfile(s);

				if (gameprofile == null) {
					throw new CommandException(Messages.PlayerHist.NOT_FOUND);
				}

				UUID uuid = gameprofile.getId();
				msg(commandsender, String.format(Messages.PlayerHist.UUID_IS, s, uuid));
				PlayerNameHistoryEntry[] pNameHistory = getPNameHistory(uuid);
				Utils.printListNumbered(commandsender, Arrays.asList(pNameHistory));
			} catch (CommandException e) {
				msg(commandsender, ChatColor.RED + e.getMessage(), "Error");
			} catch (Throwable e) {
				LOGGER.warn("Failed to fetch player name history of player " + s, e);
				msg(commandsender, ChatColor.RED + "Something went wrong:\n" + e, "Error");
			}
		}).start();
	}*/

	private void makeAnAuth(CommandSender sender, String[] passInput, Runnable callback) {
		makeAnAuth(sender, passInput, callback, false);
	}

	private void makeAnAuth(CommandSender sender, String[] passInput, Runnable callback, boolean noAG) {
		if (sender instanceof Player && pendingAuthPlayers.containsKey(sender)) {
			pendingAuthPlayers.remove(sender);
			msg(sender, "Cancelled the command.");
			return;
		}

		Authenticator authenticator = new Authenticator(sender, passInput, this, noAG);

		if (authenticator.authenticate(callback)) {
			if (sender instanceof Player) {
				pendingAuthPlayers.put((Player) sender, authenticator);
			}
		}
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		try {
			boolean isPlayer = sender instanceof Player;
			Player player = null;

			if (isPlayer) {
				player = (Player) sender;
			}

			switch (command.getName().toLowerCase()) {
				case "nameitem":
					validatePlayer(sender);

					if (args.length == 0) {
						throw new WrongUsageException(command, label);
					}

					ItemStack curitem = player.getInventory().getItemInMainHand();
					ItemMeta itemmeta = curitem.getItemMeta();
					itemmeta.setDisplayName(buildString(args, 0).replaceAll("&", "\u00a7"));
					curitem.setItemMeta(itemmeta);
					player.getInventory().setItemInMainHand(curitem);
					msg(sender, "Formatted your item name.");
					return true;

				case "colorguide":
					sender.sendMessage(new String[]{"§nMinecraft Formatting", "", "§00 §11 §22 §33", "§44 §55 §66 §77", "§88 §99 §aa §bb", "§cc §dd §ee §ff", "", "k §kMinecraft", "l §lMinecraft", "m §mMinecraft", "n §nMinecraft", "o §oMinecraft", "r §rMinecraft"});
					return true;

				case "download":
					validatePlayer(sender);
					validateOp(sender);

					if (args.length == 0) {
						throw new WrongUsageException(command, label);
					}

					URL theURL = new URL(buildString(args, 0));
					File fl = new File(getDataFolder(), "downloads");

					if (!fl.exists()) {
						fl.mkdirs();
					}

					BarColor[] cols = BarColor.values();
					final BossBar bb = getServer().createBossBar("Starting download...", cols[new Random().nextInt(cols.length)], BarStyle.SOLID);
					bb.addPlayer(player);
					final Player theP = player;
					Downloader.download(fl, theURL, 2048L * 1024L * 1024L, new DownloadListener() {
						long start;
						int oldPer;

						@Override
						public void onCompleted(File a) {
							bb.setVisible(false);
							long time = System.currentTimeMillis() - start;
							msg(theP, String.format("Downloaded %s (%s at %s/s).", a.getName(), fancyTime(time, true), formatFileSize(a.length() / time * 1000)));

							try {
								if (FilenameUtils.getExtension(a.getName()).equalsIgnoreCase("zip")) {
									theP.playSound(theP.getLocation(), "minecraft:entity.experience_orb.pickup", 1.0F, .5f);
									msg(theP, "Extracting it...");
									extractZip(a, new File(a.getParentFile(), FilenameUtils.removeExtension(a.getName())));
								}

								msg(theP, "\u00a7aOperation completed");
							} catch (Throwable e) {
								broke(e);
								msg(theP, "\u00a7cOperation failed, see above for details");
							}

							getListener(sender).sendMessage(clickBoxJson("Open file location", new ChatClickable(EnumClickAction.RUN_COMMAND, "/ls " + a.getParentFile().getAbsolutePath().replace("\\", "\\\\")), false), false);
							theP.playSound(theP.getLocation(), "minecraft:entity.player.levelup", 1.0F, .5f);
						}

						@Override
						public void onInterrupted() {
							bb.setVisible(false);
						}

						@Override
						public void onProgress(long a, long b, File c) {
							double progd = (double) a / (double) b * 1D;
							/*
							 * if (progd < 0) { System.out.println(a);
							 * System.out.println(b); }
							 */
							boolean unknownSize = b < 0;
							String name = c.getName();
							String bef = String.format("%s -", name);
							String dled = formatFileSize(a);
							String ttl = formatFileSize(b);
							int per = (int) (progd * 100D);
							if (oldPer != per) {
								theP.playSound(theP.getLocation(), "minecraft:block.note.hat", 1.0F, 2f);
							}
							oldPer = per;
							String spd = formatFileSize(a / (System.currentTimeMillis() - start) * 1000);
							bb.setProgress(unknownSize ? 0D : progd);
							if (unknownSize) {
								bb.setTitle(String.format(bef + "%s (avg. %s/s)", dled, spd));
							} else {
								bb.setTitle(String.format(bef + " %s of %s (%s%%, avg. %s/s)", dled, ttl, per, spd)); //fuck refactor
							}
						}

						@Override
						public void onStarted() {
							start = System.currentTimeMillis();
							bb.setVisible(true);
						}
					}, player);
					return true;

//				case "flyspeed":
//					validatePlayer(sender);
//					openFlySpeedGui(player);
//					return true;

				case "password":
					if (args.length == 2 || args.length == 1 && args[0].equals("clear")) {
						changePassword(sender, args);
						return true;
					} else {
						throw new WrongUsageException(command, label);
					}

					// DELETED: NameMC should do the job
//				case "playerhist":
//					if (astring.length == 0) {
//						throw new WrongUsageException("No player name", command, s);
//					}
//
//					listPNameHistory(commandsender, astring[0]);
//					return true;
				case "backupserver":
					validateOp(sender);
					//if (!(a instanceof Console))
					//throw new CommandException("This command is currently screwing up this server's machine. Use McMyAdmin instead to archive the server.");

					if (!backupRunning) {
						backupRunning = true;
						Thread thread = new Thread(() -> {
							try {
								sender.sendMessage("Starting server backup");
								long time = System.currentTimeMillis();
								zip(sender, getServer().getWorldContainer().getAbsoluteFile().getParentFile(), getTimestampedPNGFileForDirectory(getServer().getWorldContainer().getAbsoluteFile().getParentFile().getParentFile()));
								sender.sendMessage("Backup completed in " + DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - time, true, true));
							} catch (IOException e) {
								sender.sendMessage(ChatColor.RED + "Backup failed: " + e.getMessage());
							} finally {
								backupRunning = false;
							}
						}, "Server backup thread");
						thread.start();
					} else {
						throw new CommandException("The server is currently being backed up.");
					}

					return true;
				case "copyfile":
					validateOp(sender);

					if (args.length >= 2 && !args[0].equals(args[1])) {
						File file = new File(args[0]);
						File file1 = new File(args[1]);
						FileUtils.copyFile(file, file1);
						sender.sendMessage("Copied " + file.getAbsolutePath() + " to " + file1.getAbsolutePath());
					}

					return true;

//				case "gen":
//					validatePlayer(commandSender);
//					validateOp(commandSender);
//
//					if (astring.length == 0) {
//						//msg(a, "TYPE ONE OF THESE AS THE 1ST ARGUMENT: icespike, fossil");
//						return true;
//					}
//
//					sandbox();
//					msg(commandSender, "Generating feature " + astring[0].toLowerCase() + ". Expect lag for a moment.");
//					Random rng = new Random();
//					// WorldGenerator gen = null;
//					// StructureGenerator gen2 = null;
//					net.minecraft.server.v1_14_R1.World w2 = ((CraftWorld) p.getWorld()).getHandle();
//					BlockPosition pb = ((CraftPlayer) p).getHandle().getChunkCoordinates();
//					StructureBoundingBox sbb = new StructureBoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
//					// Chunk cc = p.getLocation().getChunk();
//					boolean genSuccess = false;
//					boolean hasRes = false;
//					WorldGenerator worldgenerator = null;
//					StructureGenerator structuregenerator = null;
//					int i = 0;
//					switch (astring[0].toLowerCase()) {
//						case "icepath":
//							int i2 = 3;
//
//							if (astring.length > 1) {
//								i = Math.min(Integer.parseInt(astring[1]), 30);
//							}
//
//							worldgenerator = new WorldGenPackedIce1(i2);
//							break;
//						case "icespike":
//							worldgenerator = new WorldGenPackedIce2();
//							break;
//						case "fossil":
//							worldgenerator = new WorldGenFossils();
//							break;
//						case "bonuschest":
//							worldgenerator = new WorldGenBonusChest();
//							break;
//						case "endisland":
//							worldgenerator = new WorldGenEndIsland();
//							break;
//						case "glowstone1":
//							worldgenerator = new WorldGenLightStone1();
//							break;
//						case "monument":
//							structuregenerator = new WorldGenMonument();
//							break;
//						case "tree":
//							worldgenerator = new WorldGenTrees(false);
//							break;
//						case "dungeon":
//							worldgenerator = new WorldGenDungeons();
//							break;
//						case "clay":
//							worldgenerator = new WorldGenClay(50);
//							break;
//						case "desertwell":
//							worldgenerator = new WorldGenDesertWell();
//							break;
//						case "village":
//							if (astring.length > 1) {
//								i = Math.min(25, Integer.parseInt(astring[1]));
//							}
//
//							structuregenerator = new WorldGenVillage();
//							break;
//						case "fortress":
//							structuregenerator = new WorldGenNether();
//							break;
//						case "endcity":
//							structuregenerator = new WorldGenEndCity(null);
//							break;
//						case "stronghold":
//							structuregenerator = new WorldGenStronghold();
//							break;
//						default:
//							throw new CommandException("unknown structure name! " + astring[0]);
//						case "deserttemple":
//							genSuccess = new WorldGenPyramidPiece(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
//							hasRes = true;
//							break;
//						case "witchhut":
//							genSuccess = new WorldGenWitchHut(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
//							hasRes = true;
//							break;
//						case "jungletemple":
//							genSuccess = new WorldGenJungleTemple(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
//							hasRes = true;
//							break;
//						case "igloo":
//							genSuccess = new b(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
//							hasRes = true;
//							break;
//						case "vlight":
//							genSuccess = new WorldGenVillageLight().a(w2, rng, sbb);
//							hasRes = true;
//							break;
//					}
//
//					if (!hasRes) {
//						if (structuregenerator != null) {
//							genSuccess = genStructure(structuregenerator, pb, p.getLocation().getChunk(), i);
//						}
//					} else {
//						genSuccess = worldgenerator.generate(w2, new Random(w2.getSeed()), pb);
//					}
//
//					msg(commandSender, (genSuccess ? "\u00a7aSUCCESS" : "\u00a7cFAILED") + " \u00a7rgenerating feature");
//
//					if (genSuccess) {
//						p.playSound(p.getLocation(), "minecraft:entity.item.pickup", 1.0F, 1.0f);
//					}
//
//					return true;

				case "mapgui":
					validatePlayer(sender);
					return true;

				case "guiinput":
					validatePlayer(sender);

					if (args.length < 1) {
						throw new WrongUsageException(command, label);
					}

					if (phoneMap == null) {
						throw new CommandException("Map GUI is disabled");
					}

					try {
						phoneMap.sendInput(PhoneInput.getByName(args[0].toLowerCase()), player);
					} catch (NullPointerException e) {
						throw new CommandException("Unknown input: " + args[0]);
					}

					return true;
			}
//			return WorldGen.onCommand(commandSender, b, c, astring);
			return false;
		} catch (CommandException e) {
			msg(sender, ChatColor.RED + e.getMessage(), "Error");
		} /*catch (InvocationTargetException e) {
			broke(e.getTargetException(), " in reflection");
		}*/ catch (Throwable e) {
			broke(e);
		}

		return true;
	}

	@Override
	public void onEnable() {
		Configurator.setAllLevels("", Level.ALL);
		instance = this;

		try {
			if (!SUPPORTED_NMS_VERSION.equals(getServer().getBukkitVersion())) {
				throw new RuntimeException(String.format(Messages.UNSUPPORTED, getServer().getBukkitVersion(), SUPPORTED_NMS_VERSION));
			}

			LOGGER.info("Plugin built on " + SDF.format(getBuildDate()));
			saveDefaultConfig();
			getServer().getPluginManager().registerEvents(this, this);
			battleRoyale = new BattleRoyale(this);
			configManager = new ConfigManager(this);
			banTNT = configManager.getConfig("remove_tnts");
			bcPluginErrors = configManager.getConfig("broadcast_errors");
			serverPingRecordsEnabled = configManager.getConfig("enable_server_ping_records");
			mapGuiEnabled = configManager.getConfig("enable_map_gui");
			showDamageSummary = configManager.getConfig("show_damage_summary");

			if (mapGuiEnabled.get()) {
				phoneMap = new PhoneMap();
				phoneMap.init();
			}

			if (serverPingRecordsEnabled.get()) {
				pingList = new PingList(new File(getDataFolder(), "ping-history.json"));

				try {
					pingList.load();
				} catch (Exception e) {
					LOGGER.warn("Could not load ping-history.json", e);
				}
			}

			// Register commands and permissions
			// TODO: there are 3 different methods of the executions of the commands, at least merge 1 and 2
			// 1. command declaration in plugin.yml, executor in plugin class or the executor class if getCommand().setExecutor() is called
			// DONE -> 2. commandMap.register(CustomizedPluginCommand)
			// 3. Brigadier direct register as a VanillaCommandWrapper

//			getServer().getPluginManager().addPermission(new Permission("amrsatrioserver.sysinfo", Messages.Commands.PERM_SYSINFO, PermissionDefault.OP));
//			getCommand("getbanner").setExecutor(new CommandGetBanner());
			getCommand("listfile").setExecutor(new CommandListFile());
			// I'll use NMS and Brigadier till they made a proper command API
			registerNmsCommands();
//			getServer().getScheduler().scheduleSyncDelayedTask(this, () -> fixBrigadierCommandUsages(commandMap), 5);
			// TODO afk ticker, broken
			// ticker = getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 0, 20);
			clearingExecutedCmdBlocks = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> cmdBlockList.clear(), 0, 5);
			// Initialize MCMA Bukkit compatibility
			new MCMABukkitCompat(this).init();
			// Client brand checker
			Messenger messenger = getServer().getMessenger();

			try {
				Method m = StandardMessenger.class.getDeclaredMethod("addToIncoming", PluginMessageListenerRegistration.class);
				m.setAccessible(true);
				m.invoke(messenger, new PluginMessageListenerRegistration(messenger, this, "minecraft:brand", this));
			} catch (ReflectiveOperationException e) {
				LOGGER.warn("Failed registering client brand packet handler", e);
			}
		} catch (Throwable e) {
			LOGGER.error("Caught an error while enabling this plugin, disabling!", e);
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(clearingExecutedCmdBlocks);
	}

	private void registerNmsCommands() {
		// fix stop command for McMyAdmin
		com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> vanillaDisp = MinecraftServer.getServer().vanillaCommandDispatcher.a();
		vanillaDisp.getRoot().removeCommand("stop");
		vanillaDisp.register(LiteralArgumentBuilder.<CommandListenerWrapper>literal("stop").requires(source -> source.hasPermission(4)).executes(context -> executeStop(context.getSource(), null)).then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("reason", StringArgumentType.greedyString()).executes(context -> executeStop(context.getSource(), StringArgumentType.getString(context, "reason")))));

		// register plugin commands
		registerCommand(new CommandBanExtended());
		registerCommand(new CommandBattleRoyale());
		registerCommand(new CommandBlockInformation());
		registerCommand(new CommandBuildText());
		registerCommand(new CommandConfig());
		registerCommand(new CommandDisplayLootTable());
		registerCommand(new CommandEditSign());
		registerCommand(new CommandEval());
		registerCommand(new CommandEvalSelector());
		registerCommand(new CommandExecutedCmdBlocks());
		registerCommand(new CommandGetExplorerMap());
		registerCommand(new CommandTpWorld());
		registerCommand(new CommandWelcomeTitle());
		// TODO this thing fixes only Brigadier commands registered by this plugin, make it fix the vanilla commands too
		fixBrigadierCommandUsages(((CraftServer) getServer()).getCommandMap());
	}

	private static int executeStop(CommandListenerWrapper listener, String reason) {
		listener.sendMessage(new ChatMessage("commands.stop.stopping"), true);

		if (reason != null && !reason.isEmpty()) {
			listener.sendMessage(new ChatMessage("%s: %s", "Reason", reason), true);
		}

		listener.getServer().safeShutdown(false);
		return com.mojang.brigadier.Command.SINGLE_SUCCESS;
	}

	private void registerCommand(AbstractBrigadierCommand command) {
		CraftServer server = (CraftServer) getServer();
		CommandMap commandMap = server.getCommandMap();
		CommandDispatcher dispatcher = server.getHandle().getServer().getCommandDispatcher();
		command.registerToCommandMap(commandMap, dispatcher);
	}

	private void fixBrigadierCommandUsages(CommandMap commandmap) {
		for (Map.Entry<String, Command> entry : ((CraftCommandMap) commandmap).getKnownCommands().entrySet()) {
			Command command = entry.getValue();

//			if (command instanceof VanillaCommandWrapper || command instanceof BrigadierCommandWrapper) {
			if (command instanceof VanillaCommandWrapper) {
//				CommandNode<CommandListenerWrapper> vanillaCommand = ((VanillaCommandWrapper) entry.getValue()).vanillaCommand;
				MinecraftServer server = ((CraftServer) getServer()).getServer();
				CommandListenerWrapper listener = server.getServerCommandListener();
				com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> brigadierDispatcher = server.getCommandDispatcher().a();
				String commandName = command.getName();
				ParseResults<CommandListenerWrapper> parsed = brigadierDispatcher.parse(commandName, listener);
				List<ParsedCommandNode<CommandListenerWrapper>> nodes = parsed.getContext().getNodes();

				if (nodes.isEmpty()) {
					LOGGER.warn("No CommandNode matches registered command " + commandName);
				} else {
					Map<CommandNode<CommandListenerWrapper>, String> map = brigadierDispatcher.getSmartUsage(Iterables.getLast(nodes).getNode(), listener);
					command.setUsage(map.values().stream().map(s -> "/" + commandName + " " + s).collect(Collectors.joining(";")));
				}
			}
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender commandsender, Command command, String s, String[] astring) {
		try {
			switch (command.getName().toLowerCase()) {
				case "guiinput":
					return astring.length == 1 ? Arrays.stream(PhoneInput.values()).map(phoneinput -> phoneinput.toString().toLowerCase()).collect(Collectors.toList()) : Collections.emptyList();
			}
		} catch (Throwable e) {
			broke(e);
		}

		return Collections.emptyList();
	}

	public void welcomeTitle(Player player) {
		Title title = new Title(new ChatComponentText(ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome-title")).replaceAll("%player%", player.getName())), new ChatComponentText(ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome-subtitle")).replaceAll("%player%", player.getName())), 10, 70, 20);
		title.setTimingsToTicks();
		title.send(player);
	}

	@Override
	public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
		if (s.equals("minecraft:brand")) {
			String brand = new String(bytes).substring(1);
			LOGGER.info("{0}'s client brand is {1}", player.getName(), brand);

			if (!brand.equals("vanilla")) {
				player.sendMessage(ChatColor.GRAY + String.format(Messages.MODDED_WARNING, brand));
			}
		}
	}

	public long getBuildDate() {
		try {
			FileConfiguration data = YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("plugin.yml")));
			return new SimpleDateFormat("yyyyMMdd-HHmmss").parse(data.getString("build-date")).getTime();
		} catch (Exception e) {
			LOGGER.warn("Can't get build date, returning 0", e);
			return 0;
		}
	}

//	private void openFlySpeedGui(Player a) {
//		Inventory inv = getServer().createInventory(null, 3 * 9, FLY_SPEED_CONTAINER_TITLE);
//		int i = 12;
//		ItemStack itemstack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
//		ItemMeta meta = itemstack.getItemMeta();
//		meta.setDisplayName("Slower");
//		itemstack.setItemMeta(meta);
//		inv.setItem(i++, itemstack);
//		meta.setDisplayName("Your current flying speed: " + a.getFlySpeed());
//		itemstack.setItemMeta(meta);
//		inv.setItem(i++, itemstack);
//		meta.setDisplayName("Faster");
//		itemstack.setItemMeta(meta);
//		inv.setItem(i, itemstack);
//		a.openInventory(inv);
//	}

	public static ServerPlugin getInstance() {
		return instance;
	}

	public static void msg(CommandSender a, String b) {
		msg(a, b, "Server");
	}

	public static void msg(CommandSender a, String b, String c) {
		a.sendMessage(String.format(SH_WITH_COLORS, c) + b);
	}

	public static void validateOp(CommandSender commandsender) {
		if (!commandsender.isOp()) {
			throw new CommandException("You do not have the permission to execute this command");
		}
	}

//	public void run() {
//		try {
//			long idleThreshold = 120;
//			long idleThresholdMs = idleThreshold * 1000 * 60;
//			for (Player i : getServer().getOnlinePlayers()) {
//				Object nmsS1 = getHandle(i);
//				long lastActive = (long) nmsS1.getClass().getMethod("I").invoke(nmsS1);
//
//				if (lastActive > 0L && idleThreshold > 0) {
//					long notActive = System.currentTimeMillis() - lastActive;
//					long timeUntilKick = (idleThresholdMs - notActive) / 1000 + 1;
//
//					if (timeUntilKick < 11 && idleThresholdMs - notActive >= 0) {
//						i.sendMessage(String.format("\u00a76\u00a7lYou will be AFK kicked in %d second%s", timeUntilKick, plural((int) timeUntilKick)));
//						i.playSound(i.getLocation(), "minecraft:block.note.pling", 1.0F, (timeUntilKick / 10f * 1.5f) + .5f);
//						//System.out.println(idleThresholdMs - notActive);
//					}
//
//					if (notActive > idleThresholdMs) {// TODO: show seconds until afk
//						i.kickPlayer(String.format("AFKing for %d minute%s", idleThreshold, plural((int) idleThreshold)));
//					}
//				}
//			}
//		} catch (Throwable e) {
//			LOGGER.error("Error caught in afk kicker, cancelling!", e);
//			getServer().getScheduler().cancelTask(ticker);
//		}
//	}

	public static void validatePlayer(CommandSender commandsender) {
		if (!(commandsender instanceof Player)) {
			throw new CommandException(new NonPlayerException().getMessage());
		}
	}

	private static void deprecate(CommandSender commandsender, String s, String s1, String... astring) {
		if (s.equals(s1)) {
			String[] astring1 = new String[astring.length];

			for (int i = 0; i < astring.length; i++) {
				astring1[i] = ChatColor.BOLD + "/" + astring[i] + ChatColor.RESET + SH_MSG_COLOR;
			}

			msg(commandsender, ChatColor.BOLD + "/" + s + ChatColor.RESET + SH_MSG_COLOR + " is now deprecated. Use " + joinNiceString(astring1, "or") + " instead.");
		}
	}

	private static class Damage {
		private EntityDamageEvent event;
		private long timestamp = System.currentTimeMillis();

		public Damage(EntityDamageEvent a) {
			event = a;
		}
	}

//	private static class PlayerNameHistoryEntry {
//		public String name = "Player";
//		public long changedToAt = 0;
//
//		@Override
//		public String toString() {
//			return String.format(Messages.PlayerHist.NUMBER, name, changedToAt > 0 ? String.format(Messages.PlayerHist.ON, SDF.format(changedToAt)) : Messages.PlayerHist.INITIAL_NAME);
//		}
//
//		public static class Serializer implements JsonDeserializer<PlayerNameHistoryEntry> {
//			@Override
//			public PlayerNameHistoryEntry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
//				PlayerNameHistoryEntry playernamehistoryentry = new PlayerNameHistoryEntry();
//				JsonObject jsonobject = jsonElement.getAsJsonObject();
//
//				if (jsonobject.has("name")) {
//					playernamehistoryentry.name = jsonobject.get("name").getAsString();
//				}
//
//				if (jsonobject.has("changedToAt")) {
//					playernamehistoryentry.changedToAt = jsonobject.get("changedToAt").getAsLong();
//				}
//
//				return playernamehistoryentry;
//			}
//		}
//	}
}
