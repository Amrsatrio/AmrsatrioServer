package com.amrsatrio.server;

import com.amrsatrio.server.bukkitcompat.BukkitCompat;
import com.amrsatrio.server.bukkitcompat.Executor;
import com.amrsatrio.server.mapgui.TestMapGui;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_11_R1.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.*;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftInventory;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
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
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.projectiles.BlockProjectileSource;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("deprecation")
public class AmrsatrioServer extends JavaPlugin implements Listener, Runnable, PluginMessageListener {
	//	public static final boolean DEBUG = false;
	public static final DecimalFormat HEALTH_DECIMAL_FORMAT = new DecimalFormat("#.##");
	public static final String SH_MSG_COLOR = "\u00a77";
	public static final String SERVER_HEADER = "\u00a79%s> " + SH_MSG_COLOR;
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	// private BukkitTask wsTask;
	/*
	 * public void onDisable() { System.out.println("Stopping WebServer...");
	 * Bukkit.getScheduler().cancelTask(wsTask.getTaskId());
	 * Bukkit.getScheduler(). }
	 */
//	private static final String IP_ADDRESS = Utils.getIPAddress(true);
	private static final long BUILD_DATE = getBuildDate(AmrsatrioServer.class);
	private static final String[] PW_GRADES = {"NA", "Unacceptable", "Very weak", "Poor", "Average", "Good", "Very Good", "Excellent"};
	private static final int MIN_PW_GRADE = 1;
	private static final ScriptEngine JS_ENGINE = new ScriptEngineManager().getEngineByName("js");
	public static final Logger LOGGER = LogManager.getLogger(AmrsatrioServer.class.getSimpleName());
	public static final String NMS_VERSION = Utils.getVersion2();
	public static Map<Player, PropertiesEditor> props = new HashMap<>();
	public static boolean verbose = false;
	private static final List<UUID> ALLOWED_TO_KICK_BAN_DEOP = Lists.newArrayList(UUID.fromString("77a3d6a0-49d5-45eb-bcb8-48dc26303c43"), UUID.fromString("beaa6a95-f065-4b75-883f-894488ec133e"));
	private PrintStream ps;
	//	private boolean anonMode = false;
	private boolean banTNT = false;
	private boolean chatFilterCaps = false;
	private boolean showDamageSummary = true;
	private Map<Player, Authenticator> authers = new HashMap<>();
	private int ticker;
	private Map<Player, List<Damage>> damages = new HashMap<>();
	private Map<InetAddress, String> pingName = new HashMap<>();
	private boolean isZipping;
	private static final List<String> PASSWORDED_COMMANDS = Lists.newArrayList("password", "anon", "togglebantnt", "togglechatcaps", "restart", "stop", "toggleverbose"); // , "ban", "kick", "op", "deop"

	private static long getBuildDate(Class<?> cl) {
		try {
			File f = new File(cl.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//			BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
//			return attrs.creationTime().toMillis();
			return f.lastModified();
		} catch (Exception e) {
			return 0;
		}
	}

	public static void bcm(String a) {
		Bukkit.broadcastMessage(String.format(SERVER_HEADER, "Server") + a);
	}

	public static void main(String[] a) {
	}

	public static void msg(CommandSender a, String b) {
		msg(a, b, "Server");
	}

	public static void msg(CommandSender a, String b, String c) {
		a.sendMessage(String.format(SERVER_HEADER, c) + b);
	}

	public static void msg(CommandSender a, String[] b) {
		for (String i : b)
			msg(a, i);
	}

	public static void msg(CommandSender a, String[] b, String c) {
		for (String i : b)
			msg(a, i, c);
	}

	private static String plural(int a) {
		return a == 1 ? "" : "s";
	}

	public static void sandbox() {
		throw new CommandException("This command is very risky, extra extra extra experimental, and a catastrophic failure will occur in this world upon executing this command. If you want to try this command, install this plugin outside this server.");
	}

	private static IChatBaseComponent testArrow(TippedArrow e) throws Exception {
		// TODO This is very illegal, accessing a protected method!!
		// TODO NMS Reflection, prepare for upgrade
		Object nmsS1 = Utils.getHandle(e);
		Method m = nmsS1.getClass().getDeclaredMethod("j");
		m.setAccessible(true);
		return ((net.minecraft.server.v1_11_R1.ItemStack) m.invoke(nmsS1)).C();
	}

	private static String disabledOrEnabled(boolean flag) {
		return flag ? "\u00a7aEnabled" : "\u00a7cDisabled";
	}

	private static File getTimestampedPNGFileForDirectory(File gameDirectory) {
		String s = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
		int i = 1;

		while (true) {
			File file1 = new File(gameDirectory, "Minecraft-" + s + (i == 1 ? "" : "_" + i) + ".zip");

			if (!file1.exists()) {
				return file1;
			}

			++i;
		}
	}

	private static Collection<MinecraftKey> getAvailLootTables(CommandSender commandsender) {
		List<MinecraftKey> set = new ArrayList<>();
		set.addAll(LootTables.a());

		if (!(commandsender instanceof Player)) {
			return set;
		}

		File file = new File(new File(((Player) commandsender).getWorld().getWorldFolder(), "data"), "loot_tables");

		if (!file.exists() || file.isFile()) {
			return set;
		}

		for (File file1 : file.listFiles()) {
			if (file1.isDirectory()) {
				for (File file2 : file1.listFiles()) {
					if (file2.isFile() && file2.getName().endsWith(".json")) {
						set.add(new MinecraftKey(file1.getName(), file2.getName().substring(0, file2.getName().length() - ".json".length())));
					}
				}
			}
		}

		return set;
	}

	public static void validateOp(CommandSender a) {
		if (!a.isOp()) {
			throw new CommandException("You do not have sufficient privileges to execute this command.");
		}
	}

	public static void validatePlayer(CommandSender a) {
		if (!(a instanceof Player)) {
			throw new CommandException("Only players can execute this command.");
		}
	}

	@EventHandler
	public void a(AsyncPlayerChatEvent a) throws NoSuchAlgorithmException {
		Player p = a.getPlayer();
		if (authers.containsKey(p)) {
			authers.get(p).doChat(a);
			authers.remove(p);
			a.setCancelled(true);
		} else if (props.containsKey(p)) {
			props.get(p).a(a);
		} else {
			if (chatFilterCaps && countUppercaseLetters(a.getMessage()) > 4) {
				a.setMessage(a.getMessage().toLowerCase());
				msg(p, "Minimize your usage of uppercase letters!");
			}
		}
		a.setMessage(ChatColor.translateAlternateColorCodes('&', a.getMessage()));
	}

	@EventHandler
	public void a(EntityDamageByEntityEvent a) {
		if (a.getDamager().getType() != EntityType.PLAYER && !(a.getDamager() instanceof Projectile)) {
			return;
		}
		try {
			Entity dmg = a.getDamager() instanceof Projectile && ((Projectile) a.getDamager()).getShooter() != null && ((Projectile) a.getDamager()).getShooter() instanceof Entity ? (Entity) ((Projectile) a.getDamager()).getShooter() : a.getDamager();
			//System.out.println(((Projectile) a.getDamager()).getShooter());
			if (!(dmg instanceof Player)) {
				return;
			}
			//msg(dmg, ((CraftEntity) a.getEntity()).getHandle().toString());
			IChatBaseComponent c = new ChatComponentText("\u00a7b<-- ").addSibling(damageSummary(new Damage(a), true)).addSibling(new ChatComponentText(" \u00a7b-->"));
			((CraftPlayer) dmg).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(c, (byte) 2));
		} catch (Throwable ex) {
			Utils.broke(ex);
		}
	}

	@EventHandler
	public void a(SignChangeEvent a) {
		int i = 0;
		for (String s : a.getLines())
			a.setLine(i++, ChatColor.translateAlternateColorCodes('&', s));
	}

	@EventHandler
	public void a(EntityExplodeEvent a) {
		if (banTNT && a.getEntityType() == EntityType.PRIMED_TNT) {
			int cnt = 0;
			a.setCancelled(true);
			for (Entity i : a.getEntity().getWorld().getEntitiesByClass(TNTPrimed.class))
				if (i != a.getEntity()) {
					i.remove();
					cnt++;
				}
			String msg = "\u00a7a" + a.blockList().size() + " block(s) are prevented from being destroyed!";
			if (cnt > 0) {
				msg += " " + cnt + " primed TNTs are also removed!";
			}
			for (Player i : a.getEntity().getWorld().getPlayers())
				Utils.actionBar(i, msg);
		}
	}

	@EventHandler
	public void a(InventoryClickEvent a) {
		try {
			Player player = (Player) a.getWhoClicked();
			Inventory inventory = a.getInventory();
			if (inventory.getName().equals("Change your fly speed")) {
				a.setCancelled(true);
				if (a.getSlot() == 10) {
					if (!(player.getFlySpeed() == 0.05f)) {
						//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.4f);
						player.setFlySpeed((player.getFlySpeed() * 10 - 0.05f * 10) / 10);
					} else {
						player.playSound(player.getLocation(), "minecraft:entity.item.break", 3.0f, 0.5f);
					}
				}
				if (a.getSlot() == 11) {
					//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.0f);
					player.setFlySpeed(0.1f);
				}
				if (a.getSlot() == 12) {
					if (!(player.getFlySpeed() == 1.0f)) {
						//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.4f);
						player.setFlySpeed((player.getFlySpeed() * 10 + 0.05f * 10) / 10);
					} else {
						player.playSound(player.getLocation(), "minecraft:entity.item.break", 3.0f, 0.5f);
					}
				}
				ItemStack itemstack = a.getInventory().getItem(11);
				ItemMeta itemmeta = itemstack.getItemMeta();
				itemmeta.setDisplayName("Your current fly speed: " + player.getFlySpeed());
				itemstack.setItemMeta(itemmeta);
				a.getInventory().setItem(11, itemstack);
			}
			if (props.containsKey(player)) {
				props.get(player).a(a);
			}
			if (CommandListFile.fileGuis.containsKey(player)) {
				CommandListFile.fileGuis.get(player).handle(a);
			}
			if (CommandGetBanner.openGetBanners.containsKey(player)) {
				CommandGetBanner.openGetBanners.get(player).handle(a);
			}
		} catch (Throwable e) {
			Utils.broke(e);
		}
	}

	private IInventory getLootTableInventory(Player a, String b) {
		WorldServer w = (WorldServer) Utils.getHandle(a.getWorld());
		IInventory iinventory = new InventorySubcontainer(b, true, 3 * 9);
		LootTable loottable = w.ak().a(new MinecraftKey(b));
		if (loottable == LootTable.a) {
			throw new CommandException("Non-existent loot table " + b);
		}
		loottable.a(iinventory, new Random(), new LootTableInfo.a(w).a());
		return iinventory;
	}

	@EventHandler
	public void a(InventoryCloseEvent a) {
		Player p = (Player) a.getPlayer();
		if (CommandListFile.fileGuis.containsKey(p)) {
			CommandListFile.fileGuis.get(p).handleClose(a);
		}
		if (props.containsKey(p) && !props.get(p).waiting) {
			props.remove(p);
		}
		if (CommandGetBanner.openGetBanners.containsKey(p) && !CommandGetBanner.openGetBanners.get(p).switching) {
			CommandGetBanner.openGetBanners.remove(p);
		}
	}

	public synchronized boolean a(net.minecraft.server.v1_11_R1.World world, Random random, ChunkCoordIntPair chunkcoordintpair, StructureStart structurestart) {
		int i = (chunkcoordintpair.x << 4) + 8;
		int j = (chunkcoordintpair.z << 4) + 8;
		boolean flag = false;
		if (structurestart.a() && structurestart.a(chunkcoordintpair) && structurestart.b().a(i, j, i + 15, j + 15)) {
			structurestart.a(world, random, new StructureBoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
			structurestart.b(chunkcoordintpair);
			flag = true;
		}
		return flag;
	}

	@EventHandler
	public void a(PlayerChangedWorldEvent a) {
		World from = a.getFrom();
		List<World> wl = getServer().getWorlds();
		if (a.getFrom().getPlayers().size() == 0 && from != wl.get(0) && from != wl.get(1) && from != wl.get(2)) {
			Bukkit.broadcastMessage("World " + ChatColor.BOLD + from.getName() + ChatColor.RESET + ChatColor.GRAY + " is being unloaded");
			getServer().unloadWorld(from.getName(), true);
			getServer().getWorlds().remove(from);
		}
		updateHF(a.getPlayer());
	}

	@EventHandler
	public void a(final PlayerCommandPreprocessEvent playercommandpreprocessevent) {
		final String s = playercommandpreprocessevent.getMessage().substring(1);
		String[] astring = s.split(" ", 2);
		final String label = astring[0];
		//		List<String> commandSpecificPlayers = Lists.newArrayList("ban", "kick", "op", "deop");

//		if (commandSpecificPlayers.contains(label) && !ALLOWED_TO_KICK_BAN_DEOP.contains(playercommandpreprocessevent.getPlayer().getUniqueId())) {
//		}

		if (PASSWORDED_COMMANDS.contains(label)) {
			playercommandpreprocessevent.setCancelled(true);
			makeAnAuth(playercommandpreprocessevent.getPlayer(), astring, new Runnable() {
				public void run() {
					Bukkit.dispatchCommand(playercommandpreprocessevent.getPlayer(), s);
				}
			});
		}
	}

	@EventHandler
	public void a(PlayerDeathEvent a) {
		try {
			if (!damages.containsKey(a.getEntity()) || !showDamageSummary) {
				return;
			}

			a.getEntity().sendMessage("\u00a72--- Damage summary since your last death (" + Utils.timeStringFromTicks(a.getEntity().getStatistic(Statistic.TIME_SINCE_DEATH)) + ") ---");
			int i = 1;

			for (Damage damage : damages.get(a.getEntity())) {
				((CraftPlayer) a.getEntity()).getHandle().sendMessage(new ChatComponentText("\u00a7b" + i++ + ". ").addSibling(damageSummary(damage)));
			}

			damages.remove(a.getEntity());
		} catch (Throwable e) {
			Utils.broke(e);
		}
	}

	@EventHandler
	public void a(PlayerJoinEvent playerjoinevent) {
		final Player player = playerjoinevent.getPlayer();
//		if (anonMode) {
//			String v1 = player.getAddress().getAddress().getHostAddress();
//			player.setDisplayName(v1);
//			player.setPlayerListName(v1);
//			playerjoinevent.setJoinMessage(playerjoinevent.getJoinMessage().replaceAll(player.getName(), player.getDisplayName()));
//		}

		updateHF(player);

		if (getConfig().getBoolean("welcome-state")) {
			welcomeTitle(player);
		}
//		String s = "108.61.184.122:10210";
//		if (!IP_ADDRESS.equals(s.split(":")[0])) {
//			//System.out.println(IP_ADDRESS);
//			((CraftPlayer) player).getHandle().sendMessage(new IChatBaseComponent[]{
//					new ChatComponentText("We are moving into " + s + ", which supports 16 players and 1 GB memory. We also have copied all of this server's contents into the new server. Everything else made in this server will not be copied anymore to the new server. In approximately 1 month from the new server's creation this server will be terminated and all of the contents will be deleted. Make sure you have the new server's IP in your server list.")
//							.setChatModifier(new ChatModifier().setColor(EnumChatFormat.RED).setBold(true)),
//					new ChatComponentText("Click to insert new server's IP into text box")
//							.setChatModifier(new ChatModifier().setUnderline(true).setChatClickable(new ChatClickable(ChatClickable.EnumClickAction.SUGGEST_COMMAND, s)))});
//		}
		pingName.put(player.getAddress().getAddress(), player.getName());
//		player.sendMap(mapView);
	}

	@EventHandler
	public void a(PlayerLoginEvent playerloginevent) {
		Bukkit.broadcastMessage(ChatColor.GRAY + String.format(playerloginevent.getResult() != Result.ALLOWED ? "Unfortunately, %s is refused to join the server" : "%s is joining the server", playerloginevent.getPlayer().getDisplayName()));
	}

	@EventHandler
	public void a(PlayerQuitEvent a) {
		Player player = a.getPlayer();

//		if (anonMode) {
//			a.setQuitMessage(a.getQuitMessage().replaceAll(player.getName(), player.getDisplayName()));
//		}

		if (authers.containsKey(player)) {
			authers.remove(player);
		}

		if (props.containsKey(player)) {
			props.remove(player);
		}

		if (CommandListFile.fileGuis.containsKey(player)) {
			CommandListFile.fileGuis.remove(player);
		}

		if (CommandGetBanner.openGetBanners.containsKey(player)) {
			CommandGetBanner.openGetBanners.remove(player);
		}
	}

	@EventHandler
	public void a(ServerListPingEvent serverlistpingevent) {
		try {
			/*ArrayList<String> names = new ArrayList<>();
			for (Player i : Bukkit.getOnlinePlayers())
				names.add(i.getDisplayName());
			Collections.sort(names);
			if (names.size() > 0)*/
			//serverlistpingevent.setMotd(new Random().nextBoolean() ? serverlistpingevent.getMotd() : "No MOTD? Cool!");
			InetAddress inetaddress = serverlistpingevent.getAddress();
			String player = pingName.get(inetaddress);
			String s = inetaddress.getHostAddress();
			IChatBaseComponent component1 = new ChatComponentText(s).setChatModifier(new ChatModifier().setChatClickable(new ChatClickable(ChatClickable.EnumClickAction.OPEN_URL, "http://whatismyipaddress.com/ip/" + s)).setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, new ChatComponentText("Click for more info on this IP address"))));
			IChatBaseComponent component = new ChatMessage("%s%s pinged the server", player == null ? "" : player + "@", component1).setChatModifier(new ChatModifier().setColor(EnumChatFormat.GRAY));
			((CraftServer) getServer()).getHandle().sendMessage(component);
		} catch (Throwable e) {
			LOGGER.warn("Unexpected exception occurred in ping event", e);
		}
	}

	private void changePassword(CommandSender commandsender, String[] astring) throws NoSuchAlgorithmException {
		if (astring[0].equals("clear")) {
			if (getConfig().getString("password").isEmpty()) {
				msg(commandsender, "You have not yet set a password.");
				return;
			}

			getConfig().set("password", "");
			saveConfig();
			msg(commandsender, "\u00a7aSuccessfully cleared your password. Warning: anyone can use the password protected features.");
		} else {
			if (!astring[0].equals(astring[1])) {
				msg(commandsender, "\u00a7cNew password and confirm password doesn't match.");
				Utils.tripleBeepSamePitch(commandsender, this);
				return;
			}

			int i = Authenticator.getPasswordStrength(astring[0]);

			if (i < MIN_PW_GRADE) {
				msg(commandsender, new String[]{"\u00a7cYour chosen password is too weak. Passwords should be mixed-case, contain both letters and numbers, and should ideally be more than 8 characters in length and contain non-alphanumeric characters. Certain common passwords are also prohibited.", "Strength: " + PW_GRADES[i]});
				Utils.tripleBeepSamePitch(commandsender, this);
				return;
			}

			getConfig().set("password", Utils.sha1(astring[0]));
			saveConfig();
			msg(commandsender, "\u00a7aSuccessfully changed your password. Strength: " + PW_GRADES[i]);
		}
	}

	private int countUppercaseLetters(String s) {
		int i = 0;

		for (char c : s.toCharArray()) {
			if (Character.isUpperCase(c) && Character.toLowerCase(c) != c) {
				++i;
			}
		}

		return i;
	}

	private IChatBaseComponent damageSummary(Damage damage, boolean fromMe) throws Exception {
		EntityDamageEvent entitydamageevent = damage.a;
		IChatBaseComponent icbc = new ChatComponentText(String.format("\u00a76%s:\u00a7r", StringUtils.capitalize(entitydamageevent.getCause().toString().toLowerCase().replaceAll("_", " "))));

		if (entitydamageevent instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) entitydamageevent;
			Entity damager = e.getDamager();
			String fromto = "\u00a77" + (fromMe ? "to" : "from") + "\u00a7r";
			boolean flag = true;
			//Entity fromto2 = fromMe ? e.getEntity() : damager;
			//System.out.println(fromto2);

			if (entitydamageevent.getCause() == DamageCause.PROJECTILE && ((Projectile) damager).getShooter() != null) {
				Projectile prj = (Projectile) damager;
				icbc.addSibling(new ChatMessage(" %s%s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName(), damager instanceof TippedArrow ? new ChatComponentText(" ").addSibling(testArrow((TippedArrow) damager)) : ""));
				//				if (damager instanceof TippedArrow)
				//					System.out.println(((CraftTippedArrow) damager).getHandle().effects);

				if (prj.getShooter() instanceof Entity) {
					damager = (Entity) prj.getShooter();
				} else if (prj.getShooter() instanceof BlockProjectileSource) {
					icbc.addSibling(new ChatComponentText(" \u00a77from a\u00a7r " + ((BlockProjectileSource) prj.getShooter()).getBlock().getType().toString().toLowerCase().replaceAll("_", " ")));
					flag = false;
				} else {
					flag = false;
				}
			}

			if (entitydamageevent.getCause() == DamageCause.MAGIC) {
				if (damager instanceof ThrownPotion) {
					icbc.addSibling(new ChatMessage(" %s %s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName(), ((CraftThrownPotion) damager).getHandle().getItem().C()));
					flag = false;

					if (((Projectile) damager).getShooter() != null) {
						if (((Projectile) damager).getShooter() instanceof Entity) {
							damager = (Entity) ((Projectile) damager).getShooter();
							flag = true;
						} else {
							flag = false;
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

			if (flag) {
				icbc.addSibling(new ChatMessage(" %s %s", fromto, damager != e.getEntity() ? ((CraftEntity) fromto1).getHandle().getScoreboardDisplayName() : "you"));
			}

			if (entitydamageevent.getCause() == DamageCause.FALLING_BLOCK) {
				if (damager instanceof FallingBlock) {
					icbc.addSibling(new ChatMessage(" (%s)", ((CraftFallingBlock) damager).getHandle().getBlock().getBlock().getName()));
				}
			}

			if (damager instanceof LivingEntity) {
				EntityLiving el = ((CraftLivingEntity) damager).getHandle();
				net.minecraft.server.v1_11_R1.ItemStack usingItem = el.getItemInMainHand();

				if (usingItem != null) {
					icbc.addSibling(new ChatMessage(" \u00a77using\u00a7r %s", usingItem.C()));
				}
			}
		}

		icbc.addSibling(new ChatComponentText(" \u00a7c-" + HEALTH_DECIMAL_FORMAT.format(entitydamageevent.getFinalDamage())));
		long l = System.currentTimeMillis() - damage.b;

		if (l >= 1000) {
			icbc.addSibling(new ChatComponentText(String.format(" \u00a79%ss", HEALTH_DECIMAL_FORMAT.format((double) l / 1000d))));
		}

		return icbc;
	}

	private IChatBaseComponent damageSummary(Damage damage) throws Exception {
		return damageSummary(damage, false);
	}

	private boolean genStructure(StructureGenerator a, BlockPosition b, Chunk cc, int d) throws Exception {
		Random r = new Random();
		net.minecraft.server.v1_11_R1.World w2 = ((CraftWorld) cc.getWorld()).getHandle();
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
	}

	public PlayerNameHistory[] getPNameHistory(UUID a) throws Exception {
		String raw = Utils.readUrl("https://api.mojang.com/user/profiles/" + a.toString().replaceAll("-", "") + "/names");
		if (raw.length() == 0) {
			throw new RuntimeException("The server responsed nothing. Try deleting the user cache.");
		}
		return new Gson().fromJson(raw, PlayerNameHistory[].class);
	}

	public void listPNameHistory(final CommandSender a, final String b) {
		new Thread(new Runnable() {
			public void run() {
				if (!Bukkit.getOnlineMode()) {
					msg(a, "\u00a7cThis server must be in online mode!");
					return;
				}

				msg(a, "\u00a72Fetching player name history of \u00a7l" + b + "\u00a7r...");
				GameProfile gp = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getUserCache().getProfile(b);

				if (gp == null) {
					msg(a, "\u00a7cNon-existent player: " + b);
					return;
				}

				int c = 1;

				try {
					UUID uuid = gp.getId();
					msg(a, b + "'s UUID is " + uuid);

					for (PlayerNameHistory i : getPNameHistory(uuid)) {
						String d = "\u00a76" + c++ + ".\u00a7r \u00a7e" + i.name + "\u00a7r ";
						d += (i.changedToAt > 0 ? "on \u00a7e" + SDF.format(i.changedToAt) : "initial name");
						a.sendMessage(d);
					}
				} catch (Throwable e) {
					Utils.broke(e);
				}
			}
		}).start();
	}

	private void makeAnAuth(CommandSender a, String[] d, Runnable afterThis) {
		makeAnAuth(a, d, afterThis, false);
	}

	private void makeAnAuth(CommandSender a, String[] b, Runnable c, boolean d) {
		if (a instanceof Player && authers.containsKey(a)) {
			authers.remove(a);
			msg(a, "Cancelled the command.");
			return;
		}
		Authenticator v0 = new Authenticator(a, b, this, d);
		if (v0.auth(c)) {
			if (a instanceof Player) {
				authers.put((Player) a, v0);
			}
		}
	}

	public boolean onCommand(final CommandSender commandSender, Command b, String c, final String[] astring) {
		try {
			boolean isPlayer = commandSender instanceof Player;
			Player p = null;
			if (isPlayer) {
				p = (Player) commandSender;
			}
			switch (b.getName().toLowerCase()) {
//				case "anon":
//					getConfig().set("anon-players", anon = !anon);
//					saveConfig();
//
//					for (Player i : getServer().getOnlinePlayers()) {
//						String name = anon ? i.getAddress().getAddress().getHostAddress() : i.getName();
//						i.setDisplayName(name);
//						i.setPlayerListName(name);
//					}
//
//					msg(commandSender, "Mask players with their IP address: " + disabledOrEnabled(anon));
//					return true;
//
				case "sysinfo":
					if (commandSender instanceof BlockCommandSender) {
						throw new CommandException("Command blocks are not allowed to execute this command");
					}

					validateOp(commandSender);
					String color = "\u00a77";
					commandSender.sendMessage("\u00a72--- System information ---");
					List<String> props = new ArrayList<>();
					SystemInfo sys = new SystemInfo();
					HardwareAbstractionLayer hal = sys.getHardware();
					props.add(color + "Processor: §r" + hal.getProcessor().getLogicalProcessorCount() + "x " + hal.getProcessor().getName());
					props.add(color + "RAM: §r" + Utils.freeOf(hal.getMemory().getAvailable(), hal.getMemory().getTotal()));
					OperatingSystem os = sys.getOperatingSystem();
					props.add(color + "Operating system: §r");
					props.add(color + " Name: §r" + os.getManufacturer() + " " + os.getFamily());
					props.add(color + " Version: §r" + os.getVersion());
					props.add(color + "Drives:");

					for (OSFileStore f : sys.getHardware().getFileStores()) {
						if (!f.getName().isEmpty()) {
							props.add(color + " " + f.getName() + ": §r" + Utils.freeOf(f.getUsableSpace(), f.getTotalSpace()));
						}
					}

					//props.add(Arrays.toString(sys.getHardware().getNetworkIFs()));
					commandSender.sendMessage(props.toArray(new String[props.size()]));
					return true;

				case "togglebantnt":
					getConfig().set("disable-tnt", banTNT = !banTNT);
					saveConfig();
					msg(commandSender, "Prevent TNT interacting with blocks: " + disabledOrEnabled(banTNT));
					return true;

				case "toggleverbose":
					getConfig().set("broadcast-exceptions", verbose = !verbose);
					saveConfig();
					msg(commandSender, "Broadcast plugin errors: " + disabledOrEnabled(verbose));
					return true;

				case "changeworld":
					validatePlayer(commandSender);

					if (astring.length == 0) {
						throw new WrongUsageException();
					}

					String worldname = Utils.buildString(astring, 0);
					String worldbefore = p.getWorld().getName();
					List<String> exworlds = Utils.getExistingWorlds();

					if (!exworlds.contains(worldname)) {
						msg(commandSender, "That world doesn't exist. Existing worlds are:");
						msg(commandSender, Utils.joinNiceString(exworlds.toArray(new String[exworlds.size()])));
						return true;
					}

					msg(commandSender, "Teleporting you to world \u00a7l" + worldname);
					World w = getServer().getWorld(worldname);

					if (w == null) {
						Bukkit.broadcastMessage(ChatColor.GRAY + "World " + ChatColor.BOLD + worldname + ChatColor.RESET + ChatColor.GRAY + " is being loaded");
						getServer().getWorlds().add(w = getServer().createWorld(new WorldCreator(worldname)));
					}

					p.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ()));
					msg(commandSender, "WARNING: This feature is very experimental. The scoreboard in " + worldbefore + " are mixed with the scoreboard in " + worldname + ". So don't try to teleport to lots-of-command-blocks maps!");
					return true;

				case "togglechatcaps":
					getConfig().set("chat-filter-caps", chatFilterCaps = !chatFilterCaps);
					saveConfig();
					msg(commandSender, "Transform chat messages into lowercase when more than 4 characters are uppercase: " + disabledOrEnabled(chatFilterCaps));
					return true;

				case "nameitem":
					validatePlayer(commandSender);

					if (astring.length == 0) {
						throw new WrongUsageException();
					}

					ItemStack curitem = p.getInventory().getItemInMainHand();
					ItemMeta im = curitem.getItemMeta();
					im.setDisplayName(Utils.buildString(astring, 0).replaceAll("&", "\u00a7"));
					curitem.setItemMeta(im);
					p.getInventory().setItemInMainHand(curitem);
					msg(commandSender, "Formatted your item name.");
					return true;

				case "colorguide":
					commandSender.sendMessage(new String[]{"§nMinecraft Formatting", "", "§00 §11 §22 §33", "§44 §55 §66 §77", "§88 §99 §aa §bb", "§cc §dd §ee §ff", "", "k §kMinecraft", "l §lMinecraft", "m §mMinecraft", "n §nMinecraft", "o §oMinecraft", "r §rMinecraft"});
					return true;

				case "download":
					validatePlayer(commandSender);
					validateOp(commandSender);

					if (astring.length == 0) {
						throw new WrongUsageException();
					}

					URL theURL = new URL(Utils.buildString(astring, 0));
					File fl = new File(getDataFolder(), "downloads");

					if (!fl.exists()) {
						fl.mkdirs();
					}

					BarColor[] cols = BarColor.values();
					final BossBar bb = Bukkit.createBossBar("Starting download...", cols[new Random().nextInt(cols.length)], BarStyle.SOLID);
					bb.addPlayer(p);
					final Player theP = p;
					Downloader.download(fl, theURL, 2048L * 1024L * 1024L, new Downloader.DownloadListener() {
						long start;
						int oldPer;

						@Override
						public void onCompleted(File a) {
							bb.setVisible(false);
							long time = System.currentTimeMillis() - start;
							msg(theP, String.format("Downloaded %s (%s at %s/s).", a.getName(), Utils.fancyTime(time, true), Utils.formatFileSize(a.length() / time * 1000)));
							try {
								if (FilenameUtils.getExtension(a.getName()).equalsIgnoreCase("zip")) {
									theP.playSound(theP.getLocation(), "minecraft:entity.experience_orb.pickup", 3e7f, .5f);
									msg(theP, "Extracting it...");
									Utils.extractZip(a, new File(a.getParentFile(), FilenameUtils.removeExtension(a.getName())));
								}
								msg(theP, "\u00a7aOperation completed");
							} catch (Throwable e) {
								Utils.broke(e);
								msg(theP, "\u00a7cOperation failed, see above for details");
							}
							Utils.jsonMsg(theP, Utils.clickBoxJson("Open file location", "run_command", "/ls " + a.getParentFile().getAbsolutePath().replace("\\", "\\\\")));
							theP.playSound(theP.getLocation(), "minecraft:entity.player.levelup", 3e7f, .5f);
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
							String dled = Utils.formatFileSize(a);
							String ttl = Utils.formatFileSize(b);
							int per = (int) (progd * 100D);
							if (oldPer != per) {
								theP.playSound(theP.getLocation(), "minecraft:block.note.hat", 3e7f, 2f);
							}
							oldPer = per;
							String spd = Utils.formatFileSize(a / (System.currentTimeMillis() - start) * 1000);
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
					}, p);
					return true;

				case "editsign":
					validatePlayer(commandSender);
					RayTrace raytrace = RayTrace.a(p, 5.0D);

					if (raytrace.a() && (raytrace.c().getType() == Material.SIGN_POST || raytrace.c().getType() == Material.WALL_SIGN)) {
						/*if (astring.length < 3) throw new WrongUsageException();
						int line = CommandAbstract.a(astring[0], 1, 4) - 1;
						ste.lines[line] = ste.lines[line];*/
						EntityPlayer entityplayer = ((CraftPlayer) p).getHandle();
						TileEntitySign tileentitysign = (TileEntitySign) entityplayer.getWorld().getTileEntity(raytrace.f());
						tileentitysign.isEditable = true;
						tileentitysign.a(entityplayer);
						entityplayer.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(raytrace.f()));
					} else {
						msg(commandSender, "You're not pointing at a sign.");
					}

					return true;

				case "eval":
					validateOp(commandSender);

					if (astring.length == 0) {
						throw new WrongUsageException();
					}

					String code = Utils.buildString(astring, 0);
					commandSender.sendMessage("\u00a77<- " + code);

					try {
						commandSender.sendMessage("-> " + JS_ENGINE.eval(code));
					} catch (ScriptException e) {
						commandSender.sendMessage("\u00a7c" + e.getMessage().replaceAll("\r", "\n"));
					}

					return true;

				case "flyspeed":
					validatePlayer(commandSender);
					openFlySpeedGui(p);
					return true;

				case "loottable":
					validatePlayer(commandSender);
					if (astring.length == 0) {
						throw new WrongUsageException();
					}
					if (p.getGameMode() != GameMode.CREATIVE) {
						throw new CommandException("No taking items from loot tables!");
					}
					IInventory inv = getLootTableInventory(p, astring[0]);
					if (astring.length == 2 && astring[1].equals("true")) {
						InventoryUtils.dropEntity(((CraftWorld) p.getWorld()).getHandle(), ((CraftPlayer) p).getHandle(), inv);
					} else {
						p.openInventory(new CraftInventory(inv));
					}
					return true;

				case "password":
					if (astring.length == 2 || (astring.length == 1 && astring[0].equals("clear"))) {
						changePassword(commandSender, astring);
						return true;
					} else {
						throw new WrongUsageException();
					}
//				case "pathdebug":
//					throw new CommandException("I'm now still figuring out on how to re-create the debug path things like in the 1.9.2 snapshots.");
				case "playerhist":
					if (astring.length == 0) {
						throw new WrongUsageException("No player name");
					}

					listPNameHistory(commandSender, astring[0]);
					return true;
//				case "selector":
//					if (astring.length == 0) throw new WrongUsageException();
//					Utils.noReflection(commandSender);
//					List<net.minecraft.server.v1_11_R1.Entity> s = Selector.select(commandSender, astring[0]);
//					// List<String> list = new ArrayList<>();
//					// for (Object i : s)
//					// list.add(i.toString());
//					msg(commandSender, "Expression " + astring[0] + " matches " + s.size() + " entities");
//					// msg(a, "DEBUG " + Utils.getListener(a).a(1, "@"));
//					throw new CommandException("This command is broken!");
				case "text":
					validatePlayer(commandSender);

					if (astring.length == 0) {
						throw new WrongUsageException("Missing text argument");
					}

					StringBuilder text = new StringBuilder();

					for (String i : astring) {
						text.append(i);
						text.append(" ");
					}

					return Texter.text(text.toString().trim(), p, false);

				case "toggledmgsummary":
					getConfig().set("show-damage-summary", showDamageSummary = !showDamageSummary);
					saveConfig();
					msg(commandSender, "Show damage summary for all players: " + disabledOrEnabled(showDamageSummary));
					return true;

				case "wsubtitle":
					validateOp(commandSender);
					String ttl = Utils.buildString(astring, 0);
					getConfig().set("welcome-subtitle", ttl);
					saveConfig();
					msg(commandSender, "Saved the welcome subtitle text. " + ttl.replaceAll("&", "\u00a7"));
					return true;

				case "wtest":
					if (!isPlayer || (astring.length == 1 && astring[0].equals("-t"))) {
						msg(commandSender, new String[]{"This is the welcome title message:", getConfig().getString("welcome-title").replaceAll("&", "\u00a7"), getConfig().getString("welcome-subtitle").replaceAll("&", "\u00a7")});
						return true;
					}

					welcomeTitle(p);
					msg(commandSender, "That's the welcome title.");
					return true;

				case "wtitle":
					validateOp(commandSender);
					String stl = Utils.buildString(astring, 0);
					getConfig().set("welcome-title", stl);
					saveConfig();
					msg(commandSender, "Saved the welcome title text. " + stl.replaceAll("&", "\u00a7"));
					return true;

				case "wtoggle":
					validateOp(commandSender);
					boolean state = getConfig().getBoolean("welcome-state");
					getConfig().set("welcome-state", state = !state);
					saveConfig();
					msg(commandSender, "Show welcome title to joined players: " + disabledOrEnabled(state));
					return true;

				case "zipserver":
					validateOp(commandSender);
					//if (!(a instanceof Console))
					//throw new CommandException("This command is currently screwing up this server's machine. Use McMyAdmin instead to archive the server.");
					if (!isZipping) {
						isZipping = true;
						new Thread(new Runnable() {
							public void run() {
								try {
									commandSender.sendMessage("Starting zip of the server folder");
									long time = System.currentTimeMillis();
									Utils.zip(commandSender, Bukkit.getWorldContainer().getAbsoluteFile().getParentFile(), getTimestampedPNGFileForDirectory(Bukkit.getWorldContainer().getAbsoluteFile().getParentFile().getParentFile()));
									commandSender.sendMessage("Zip completed in " + DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - time, true, true));
								} catch (Throwable e) {
									Utils.broke(e);
								}
								isZipping = false;
							}
						}).start();
					} else {
						commandSender.sendMessage("§cArchiving the server is currently in progress.");
					}

					return true;
				case "gen":
					validatePlayer(commandSender);
					validateOp(commandSender);

					if (astring.length == 0) {
						//msg(a, "TYPE ONE OF THESE AS THE 1ST ARGUMENT: icespike, fossil");
						return true;
					}

					sandbox();
					msg(commandSender, "Generating feature " + astring[0].toLowerCase() + ". Expect lag for a moment.");
					Random rng = new Random();
					// WorldGenerator gen = null;
					// StructureGenerator gen2 = null;
					net.minecraft.server.v1_11_R1.World w2 = ((CraftWorld) p.getWorld()).getHandle();
					BlockPosition pb = ((CraftPlayer) p).getHandle().getChunkCoordinates();
					StructureBoundingBox sbb = new StructureBoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
					// Chunk cc = p.getLocation().getChunk();
					boolean genSuccess = false;
					boolean hasRes = false;
					WorldGenerator worldgenerator = null;
					StructureGenerator structuregenerator = null;
					int i = 0;
					switch (astring[0].toLowerCase()) {
						case "icepath":
							int i2 = 3;

							if (astring.length > 1) {
								i = Math.min(Integer.parseInt(astring[1]), 30);
							}

							worldgenerator = new WorldGenPackedIce1(i2);
							break;
						case "icespike":
							worldgenerator = new WorldGenPackedIce2();
							break;
						case "fossil":
							worldgenerator = new WorldGenFossils();
							break;
						case "bonuschest":
							worldgenerator = new WorldGenBonusChest();
							break;
						case "endisland":
							worldgenerator = new WorldGenEndIsland();
							break;
						case "glowstone1":
							worldgenerator = new WorldGenLightStone1();
							break;
						case "monument":
							structuregenerator = new WorldGenMonument();
							break;
						case "tree":
							worldgenerator = new WorldGenTrees(false);
							break;
						case "dungeon":
							worldgenerator = new WorldGenDungeons();
							break;
						case "clay":
							worldgenerator = new WorldGenClay(50);
							break;
						case "desertwell":
							worldgenerator = new WorldGenDesertWell();
							break;
						case "village":
							if (astring.length > 1) {
								i = Math.min(25, Integer.parseInt(astring[1]));
							}

							structuregenerator = new WorldGenVillage();
							break;
						case "fortress":
							structuregenerator = new WorldGenNether();
							break;
						case "endcity":
							structuregenerator = new WorldGenEndCity(null);
							break;
						case "stronghold":
							structuregenerator = new WorldGenStronghold();
							break;
						default:
							throw new CommandException("unknown structure name! " + astring[0]);
						case "deserttemple":
							genSuccess = new WorldGenRegistration.WorldGenPyramidPiece(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
							hasRes = true;
							break;
						case "witchhut":
							genSuccess = new WorldGenRegistration.WorldGenWitchHut(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
							hasRes = true;
							break;
						case "jungletemple":
							genSuccess = new WorldGenRegistration.WorldGenJungleTemple(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
							hasRes = true;
							break;
						case "igloo":
							genSuccess = new WorldGenRegistration.b(rng, pb.getX(), pb.getZ()).a(w2, rng, sbb);
							hasRes = true;
							break;
						case "vlight":
							genSuccess = new WorldGenVillagePieces.WorldGenVillageLight().a(w2, rng, sbb);
							hasRes = true;
							break;
					}

					if (!hasRes) {
						if (structuregenerator != null) {
							genSuccess = genStructure(structuregenerator, pb, p.getLocation().getChunk(), i);
						}
					} else {
						genSuccess = worldgenerator.generate(w2, new Random(w2.getSeed()), pb);
					}

					msg(commandSender, (genSuccess ? "\u00a7aSUCCESS" : "\u00a7cFAILED") + " \u00a7rgenerating feature");

					if (genSuccess) {
						p.playSound(p.getLocation(), "minecraft:entity.item.pickup", 3e7f, 1.0f);
					}

					return true;

				case "mapgui":
					validatePlayer(commandSender);
					return true;
			}
//			return WorldGen.onCommand(commandSender, b, c, astring);
			return false;
		} catch (CommandException e) {
			msg(commandSender, "\u00a7c" + e.getMessage());
			return true;
		} catch (WrongUsageException e) {
			msg(commandSender, new String[]{"Bad usage", "Usage: " + b.getUsage().replaceFirst("<command>", c)});
			return true;
		} catch (InvocationTargetException e) {
			Utils.broke(e.getTargetException(), " in reflection");
			return true;
		} catch (Throwable e) {
			Utils.broke(e);
			return true;
		}
	}

	// @SuppressWarnings("unused")
	public void onEnable() {
		try {
			LOGGER.info("NMS version: " + NMS_VERSION);
			LOGGER.info("Plugin built on " + SDF.format(BUILD_DATE));

			if (!"v1_11_R1".equals(NMS_VERSION)) {
				throw new RuntimeException("This plugin does not support your version of Minecraft! You have to tell me to update this plugin, or downgrade to v1_11_R1.");
			}

			saveDefaultConfig();
			Bukkit.getPluginManager().registerEvents(this, this);
//			anonMode = getConfig().getBoolean("anon");
			banTNT = getConfig().getBoolean("disable-tnt");
			chatFilterCaps = getConfig().getBoolean("chat-filter-caps");
			showDamageSummary = getConfig().getBoolean("show-damage-summary");
			// webserver, currently very broken
			/*
			 * wsTask = Bukkit.getScheduler().runTaskAsynchronously(this, new
			 * Runnable() { public void run() { WebServer ws = new WebServer();
			 * ws.start(); } });
			 */
			new TestMapGui().init();

			getCommand("getbanner").setExecutor(new CommandGetBanner());
			getCommand("listfile").setExecutor(new CommandListFile());
			getCommand("blockinfo").setExecutor(new CommandCoords());
			getCommand("banadv").setExecutor(new CommandBanAdvanced());
			//TODO afk ticker
//			ticker = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this, 0, 20);
			// faking MCMA compat
			this.ps = new PrintStream(new FileOutputStream(FileDescriptor.err));
			Bukkit.getPluginManager().registerEvents(new BukkitCompat(), this);
			/*
			 * if (false) { getCommand("tell").setExecutor(new Executor(ps));
			 * getCommand("kickreason").setExecutor(new Executor(ps)); }
			 */
			getCommand("svping").setExecutor(new Executor(ps));
			getCommand("pushcommand").setExecutor(new Executor(ps));
			this.ps.println("0 0 [MCMAX] MCMACOMPAT R22A");
//			WorldGen.onEnable(this);
			Bukkit.getMessenger().registerIncomingPluginChannel(this, "MC|Brand", this);
		} catch (Throwable e) {
			LOGGER.error("Caught an error when enabling this plugin, this plugin will be disabled!", e);
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	public List<String> onTabComplete(CommandSender commandsender, Command command, String s, String[] astring) {
		try {
			switch (command.getName().toLowerCase()) {
				case "changeworld":
					return CommandAbstract.a(astring, Utils.getExistingWorlds());
				case "loottable":
					return astring.length == 1 ? CommandAbstract.a(astring, getAvailLootTables(commandsender)) : astring.length == 2 ? CommandAbstract.a(astring, Arrays.asList("true", "false")) : Collections.<String>emptyList();
			}
		} catch (Throwable e) {
			Utils.broke(e);
		}

		return Collections.emptyList();
	}

	public void openFlySpeedGui(Player a) {
		Inventory inv = Bukkit.createInventory(null, 3 * 9, "Change your fly speed");
		int i = 10;
		ItemStack itemstack = new ItemStack(Material.DIAMOND_PICKAXE, 1);
		ItemMeta meta = itemstack.getItemMeta();
		meta.setDisplayName("Slower");
		itemstack.setItemMeta(meta);
		inv.setItem(i++, itemstack);
		meta.setDisplayName("Your current flying speed: " + a.getFlySpeed());
		itemstack.setItemMeta(meta);
		inv.setItem(i++, itemstack);
		meta.setDisplayName("Faster");
		itemstack.setItemMeta(meta);
		inv.setItem(i, itemstack);
		a.openInventory(inv);
	}

	@EventHandler
	public void onDamage(EntityDamageEvent a) {
		if (a.getEntityType() != EntityType.PLAYER || !showDamageSummary) {
			return;
		}

		Player e = (Player) a.getEntity();
		List<Damage> toSet = damages.containsKey(e) ? damages.get(e) : new ArrayList<Damage>();
		Damage d = new Damage(a);
		toSet.add(d);
		damages.put(e, toSet);

		try {
			((CraftPlayer) a.getEntity()).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(damageSummary(d), (byte) 2));
		} catch (Throwable ex) {
			Utils.broke(ex);
		}
	}

	public void run() {
		try {
			long idleThreshold = 120;
			long idleThresholdMs = idleThreshold * 1000 * 60;
			for (Player i : Bukkit.getOnlinePlayers()) {
				Object nmsS1 = Utils.getHandle(i);
				long lastActive = (long) nmsS1.getClass().getMethod("I").invoke(nmsS1);

				if (lastActive > 0L && idleThreshold > 0) {
					long notActive = System.currentTimeMillis() - lastActive;
					long timeUntilKick = (idleThresholdMs - notActive) / 1000 + 1;

					if (timeUntilKick < 11 && idleThresholdMs - notActive >= 0) {
						i.sendMessage(String.format("\u00a76\u00a7lYou will be AFK kicked in %d second%s", timeUntilKick, plural((int) timeUntilKick)));
						i.playSound(i.getLocation(), "minecraft:block.note.pling", 3e7f, (timeUntilKick / 10f * 1.5f) + .5f);
						//System.out.println(idleThresholdMs - notActive);
					}

					if (notActive > idleThresholdMs) {// TODO: show seconds until afk
						i.kickPlayer(String.format("AFKing for %d minute%s", idleThreshold, plural((int) idleThreshold)));
					}
				}
			}
		} catch (Throwable e) {
			LOGGER.error("Error caught in afk kicker, cancelling!", e);
			Bukkit.getScheduler().cancelTask(ticker);
		}
	}

	private void updateHF(Player a) {
		Utils.tabHeaderFooter(a, "\u00a7aWelcome to\n\u00a7l" + Bukkit.getServerName(), "\u00a76You're currently in\n\u00a7l" + a.getWorld().getName());
	}

	public void welcomeTitle(Player player) {
		Title title = new Title(getConfig().getString("welcome-title").replaceAll("%player%", player.getName()), getConfig().getString("welcome-subtitle").replaceAll("%player%", player.getName()), 10, 70, 20);
		title.setTimingsToTicks();
		title.send(player);
	}

	@Override
	public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
		//Bukkit.broadcastMessage(s);
		//Bukkit.broadcastMessage(player.toString());

		if (s.equals("MC|Brand")) {
			String brand = new String(bytes).substring(1);

			if (!brand.equals("vanilla")) {
				player.sendMessage(ChatColor.GRAY + "We've detected that you're using a modded version of Minecraft, which is \"" + brand + "\". You're allowed to use the mods as long as you play fair!");
			}
		}
	}

	private static class Damage {
		private EntityDamageEvent a;
		private long b = System.currentTimeMillis();

		public Damage(EntityDamageEvent a) {
			this.a = a;
		}
	}

	private static class PlayerNameHistory {
		String name = "Player";
		long changedToAt = 0;
	}
}
