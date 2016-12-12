package com.amrsatrio.server;

import com.amrsatrio.server.Downloader.DownloadListener;
import com.amrsatrio.server.PingList.PingEntry;
import com.amrsatrio.server.bukkitcompat.MCMABukkitCompat;
import com.amrsatrio.server.command.*;
import com.amrsatrio.server.command.CommandGetBanner.GetBannerGui;
import com.amrsatrio.server.mapphone.PhoneMap;
import com.amrsatrio.server.mapphone.PhoneMap.PhoneInput;
import com.amrsatrio.server.nmscommand.CommandTestSelector;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_11_R1.*;
import net.minecraft.server.v1_11_R1.ChatClickable.EnumClickAction;
import net.minecraft.server.v1_11_R1.ChatHoverable.EnumHoverAction;
import net.minecraft.server.v1_11_R1.LootTableInfo.a;
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
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.command.VanillaCommandWrapper;
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
import org.bukkit.event.player.PlayerPreLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.projectiles.BlockProjectileSource;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.amrsatrio.server.Utils.*;

public class ServerPlugin extends JavaPlugin implements Listener, PluginMessageListener {
	//	public static final boolean DEBUG = false;
	public static final DecimalFormat HEALTH_DECIMAL_FORMAT = new DecimalFormat("#.##");
	public static final ChatColor SH_COLOR = ChatColor.BLUE;
	public static final ChatColor SH_MSG_COLOR = ChatColor.GRAY;
	public static final String SH = "%s> ";
	public static final String SH_WITH_COLORS = SH_COLOR + SH + SH_MSG_COLOR;
	public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	public static final Logger LOGGER = LogManager.getLogger(ServerPlugin.class.getSimpleName());
	public static final String NMS_VERSION = getVersion2();
	public static final String SUPPORTED_NMS_VERSION = "v1_11_R1";
	private static final String[] PW_GRADES = {"NA", "Unacceptable", "Very weak", "Poor", "Average", "Good", "Very Good", "Excellent"};
	private static final int MIN_PW_GRADE = 1;
	private static final ScriptEngine JS_ENGINE = new ScriptEngineManager().getEngineByName("js");
	//	private static final List<UUID> ALLOWED_TO_KICK_BAN_DEOP = Lists.newArrayList(UUID.fromString("77a3d6a0-49d5-45eb-bcb8-48dc26303c43"), UUID.fromString("beaa6a95-f065-4b75-883f-894488ec133e"));
	private static final List<String> PASSWORDED_COMMANDS = Lists.newArrayList("password", /*"anon", */"togglebantnt", "togglechatcaps", "restart", "stop", "toggleverbose", "toggledmgsummary"); // , "ban", "kick", "op", "deop"
	private static final Function<GameProfile, IChatBaseComponent> GAME_PROFILE_TO_NAME_FUNCTION = new Function<GameProfile, IChatBaseComponent>() {
		@Nullable
		@Override
		public IChatBaseComponent apply(@Nullable GameProfile gameprofile) {
			return new ChatComponentText(gameprofile.getName()).setChatModifier(new ChatModifier().setChatHoverable(new ChatHoverable(EnumHoverAction.SHOW_TEXT, new ChatComponentText(gameprofile.getName() + "\n" + ChatColor.GRAY + gameprofile.getId()))).setChatClickable(new ChatClickable(EnumClickAction.SUGGEST_COMMAND, gameprofile.getId().toString())));
		}
	};
	private static final Gson PLAYER_HIST_ENTRY_JSON = new GsonBuilder().registerTypeAdapter(PlayerNameHistoryEntry.class, new PlayerNameHistoryEntry.Serializer()).create();
	public static Map<Player, PropertiesEditor> propEditInstances = new HashMap<>();
	public static Map<Player, Authenticator> authenticatorInstances = new HashMap<>();
	public static Map<Player, GetBannerGui> getBannerInstances = new HashMap<>();
	private final PhoneMap phoneMap = new PhoneMap();
	private static ServerPlugin instance;
	// private BukkitTask wsTask;
	/*
	 * public void onDisable() { System.out.println("Stopping WebServer...");
	 * Bukkit.getScheduler().cancelTask(wsTask.getTaskId());
	 * Bukkit.getScheduler(). }
	 */
//	private static final String IP_ADDRESS = getIPAddress(true);
	private long buildDate;
	//	private boolean anonMode = false;
	private boolean banTNT = false;
	private boolean chatFilterCaps = false;
	private boolean showDamageSummary = true;
	public boolean verbose = false;
	private int ticker;
	private Map<Player, List<Damage>> playerDamages = new HashMap<>();
	private boolean isZipping;
	public PingList pingList;

	public static ServerPlugin getInstance() {
		return instance;
	}

//	public static void bcm(String a) {
//		Bukkit.broadcastMessage(String.format(SH_WITH_COLORS, "Server") + a);
//	}

	public static void msg(CommandSender a, String b) {
		msg(a, b, "Server");
	}

	public static void msg(CommandSender a, String b, String c) {
		a.sendMessage(String.format(SH_WITH_COLORS, c) + b);
	}

	public static void msg(CommandSender a, String... b) {
		for (String i : b) {
			msg(a, i);
		}
	}

//	public static void msg(CommandSender a, String[] b, String c) {
//		for (String i : b) {
//			msg(a, i, c);
//		}
//	}

	public static void validateOp(CommandSender commandsender) {
		if (!commandsender.isOp()) {
			throw new CommandException("You do not have sufficient privileges to execute this command");
		}
	}

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

	@EventHandler
	public void a(AsyncPlayerChatEvent asyncplayerchatevent) throws NoSuchAlgorithmException {
		Player player = asyncplayerchatevent.getPlayer();

		if (authenticatorInstances.containsKey(player)) {
			authenticatorInstances.get(player).doChat(asyncplayerchatevent);
			authenticatorInstances.remove(player);
			asyncplayerchatevent.setCancelled(true);
		} else if (propEditInstances.containsKey(player)) {
			propEditInstances.get(player).a(asyncplayerchatevent);
		} else {
			if (chatFilterCaps && countUppercaseLetters(asyncplayerchatevent.getMessage()) > 4) {
				asyncplayerchatevent.setMessage(asyncplayerchatevent.getMessage().toLowerCase());
				msg(player, Messages.ChatCaps.WARN_MINIMIZE_CAPS);
			}
		}

		asyncplayerchatevent.setMessage(ChatColor.translateAlternateColorCodes('&', asyncplayerchatevent.getMessage()));
	}

	@EventHandler
	public void a(EntityDamageByEntityEvent entitydamagebyentityevent) {
		if (entitydamagebyentityevent.getDamager().getType() != EntityType.PLAYER && !(entitydamagebyentityevent.getDamager() instanceof Projectile) || !showDamageSummary) {
			return;
		}

		try {
			Entity entity = entitydamagebyentityevent.getDamager() instanceof Projectile && ((Projectile) entitydamagebyentityevent.getDamager()).getShooter() != null && ((Projectile) entitydamagebyentityevent.getDamager()).getShooter() instanceof Entity ? (Entity) ((Projectile) entitydamagebyentityevent.getDamager()).getShooter() : entitydamagebyentityevent.getDamager();
			//System.out.println(((Projectile) entitydamagebyentityevent.getDamager()).getShooter());

			if (!(entity instanceof Player)) {
				return;
			}

			//msg(entity, ((CraftEntity) entitydamagebyentityevent.getEntity()).getHandle().toString());
			IChatBaseComponent ichatbasecomponent = new ChatComponentText("\u00a7b<-- ").addSibling(damageSummary(new Damage(entitydamagebyentityevent), true)).a(" \u00a7b-->");
			actionBarWithoutReflection((Player) entity, ichatbasecomponent);
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
		if (banTNT && entityexplodeevent.getEntityType() == EntityType.PRIMED_TNT) {
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
				actionBarWithoutReflection(player, stringbuilder.toString());
			}
		}
	}

	@EventHandler
	public void a(InventoryClickEvent inventoryclickevent) {
		try {
			Player player = (Player) inventoryclickevent.getWhoClicked();
			Inventory inventory = inventoryclickevent.getInventory();

			if (inventory.getName().equals("Change your fly speed")) {
				inventoryclickevent.setCancelled(true);
				if (inventoryclickevent.getSlot() == 10) {
					if (!(player.getFlySpeed() == 0.05f)) {
						//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.4f);
						player.setFlySpeed((player.getFlySpeed() * 10 - 0.05f * 10) / 10);
					} else {
						player.playSound(player.getLocation(), "minecraft:entity.item.break", 3.0f, 0.5f);
					}
				}

				if (inventoryclickevent.getSlot() == 11) {
					//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.0f);
					player.setFlySpeed(0.1f);
				}

				if (inventoryclickevent.getSlot() == 12) {
					if (!(player.getFlySpeed() == 1.0f)) {
						//p.playSound(p.getLocation(), "minecraft:block.note.pling", 3.0f, 1.4f);
						player.setFlySpeed((player.getFlySpeed() * 10 + 0.05f * 10) / 10);
					} else {
						player.playSound(player.getLocation(), "minecraft:entity.item.break", 3.0f, 0.5f);
					}
				}

				ItemStack itemstack = inventoryclickevent.getInventory().getItem(11);
				ItemMeta itemmeta = itemstack.getItemMeta();
				itemmeta.setDisplayName("Your current fly speed: " + player.getFlySpeed());
				itemstack.setItemMeta(itemmeta);
				inventoryclickevent.getInventory().setItem(11, itemstack);
			}

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

	private IInventory getLootTableInventory(Player player, String s) {
		WorldServer worldserver = ((CraftWorld) player.getWorld()).getHandle();
		IInventory iinventory = new InventorySubcontainer(s, true, 3 * 9);
		LootTable loottable = worldserver.ak().a(new MinecraftKey(s));

		if (loottable == LootTable.a) {
			throw new CommandException("Non-existent loot table " + s);
		}

		loottable.a(iinventory, new Random(), new a(worldserver).a());
		return iinventory;
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
		//		List<String> commandSpecificPlayers = Lists.newArrayList("ban", "kick", "op", "deop");

//		if (commandSpecificPlayers.contains(label) && !ALLOWED_TO_KICK_BAN_DEOP.contains(playercommandpreprocessevent.getPlayer().getUniqueId())) {
//		}
		deprecate(playercommandpreprocessevent.getPlayer(), label, "changeworld", "tpworld", "tpw");

		if (PASSWORDED_COMMANDS.contains(label)) {
			playercommandpreprocessevent.setCancelled(true);
			makeAnAuth(playercommandpreprocessevent.getPlayer(), astring, new Runnable() {
				@Override
				public void run() {
					Bukkit.dispatchCommand(playercommandpreprocessevent.getPlayer(), s);
				}
			});
		}
	}

	@EventHandler
	public void a(PlayerDeathEvent playerdeathevent) {
		try {
			if (!playerDamages.containsKey(playerdeathevent.getEntity()) || !showDamageSummary) {
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

		pingList.addEntry(player);
	}

	@EventHandler
	public void a(AsyncPlayerPreLoginEvent asyncplayerpreloginevent) {
		Result result = asyncplayerpreloginevent.getResult();
		String s = asyncplayerpreloginevent.getName();
		Bukkit.broadcastMessage(ChatColor.GRAY + (result != Result.ALLOWED ? String.format("Unfortunately, %s is refused to log in because %s", s, getRefuseMessage(result)) : s + " is logging in..."));
	}

	@EventHandler
	public void a(PlayerQuitEvent a) {
		Player player = a.getPlayer();

//		if (anonMode) {
//			a.setQuitMessage(a.getQuitMessage().replaceAll(player.getName(), player.getDisplayName()));
//		}

		if (authenticatorInstances.containsKey(player)) {
			authenticatorInstances.remove(player);
		}

		if (propEditInstances.containsKey(player)) {
			propEditInstances.remove(player);
		}

		if (CommandListFile.fileGuis.containsKey(player)) {
			CommandListFile.fileGuis.remove(player);
		}

		if (getBannerInstances.containsKey(player)) {
			getBannerInstances.remove(player);
		}
	}

	@EventHandler
	public void a(ServerListPingEvent serverlistpingevent) {
		try {
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
		} catch (Throwable e) {
			LOGGER.warn("Unexpected error occurred in ping event", e);
		}
	}

	private IChatBaseComponent getPingedPlayerNames(PingEntry pingentry) {
		if (pingentry == null || pingentry.names.isEmpty()) {
			return null;
		}

		String[] astring = new String[pingentry.names.size()];
		Arrays.fill(astring, "%s");
		return new ChatMessage(joinNiceString(astring, "or"), Lists.transform(new ArrayList<>(pingentry.names), GAME_PROFILE_TO_NAME_FUNCTION).toArray(new IChatBaseComponent[pingentry.names.size()]));
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

	private static int countUppercaseLetters(String s) {
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
			String fromTo = "\u00a77" + (fromMe ? "to" : "from") + "\u00a7r";
			boolean flag = true;
			//Entity fromto2 = fromMe ? e.getEntity() : damager;
			//System.out.println(fromto2);

			if (entitydamageevent.getCause() == DamageCause.PROJECTILE && ((Projectile) damager).getShooter() != null) {
				Projectile prj = (Projectile) damager;
				icbc.addSibling(new ChatMessage(" %s%s", ((CraftEntity) damager).getHandle().getScoreboardDisplayName(), damager instanceof TippedArrow ? new ChatComponentText(" ").addSibling(getTippedArrowItem((TippedArrow) damager).C()) : ""));
				//				if (damager instanceof TippedArrow)
				//					System.out.println(((CraftTippedArrow) damager).getHandle().effects);

				if (prj.getShooter() instanceof Entity) {
					damager = (Entity) prj.getShooter();
				} else if (prj.getShooter() instanceof BlockProjectileSource) {
					icbc.a(" \u00a77from a\u00a7r " + ((BlockProjectileSource) prj.getShooter()).getBlock().getType().toString().toLowerCase().replaceAll("_", " "));
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
				icbc.addSibling(new ChatMessage(" %s %s", fromTo, damager != e.getEntity() ? ((CraftEntity) fromto1).getHandle().getScoreboardDisplayName() : "you"));
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

		icbc.a(" \u00a7c-" + HEALTH_DECIMAL_FORMAT.format(entitydamageevent.getFinalDamage()));
		long l = System.currentTimeMillis() - damage.b;

		if (l >= 1000) {
			icbc.a(String.format(" \u00a79%ss", HEALTH_DECIMAL_FORMAT.format((double) l / 1000d)));
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

	public PlayerNameHistoryEntry[] getPNameHistory(UUID a) throws IOException {
		String s = readUrl("https://api.mojang.com/user/profiles/" + a.toString().replaceAll("-", "") + "/names");

		if (s.length() == 0) {
			throw new RuntimeException("The server responded nothing. Try deleting the user cache.");
		}

		return PLAYER_HIST_ENTRY_JSON.fromJson(s, PlayerNameHistoryEntry[].class);
	}

	public void listPNameHistory(final CommandSender commandsender, final String s) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (!Bukkit.getOnlineMode()) {
						throw new CommandException(Messages.PlayerHist.OFF_MODE);
					}

					msg(commandsender, String.format(Messages.PlayerHist.HEADER, s));
					GameProfile gameprofile = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getUserCache().getProfile(s);

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
			}
		}).start();
	}

	private void makeAnAuth(CommandSender a, String[] d, Runnable afterThis) {
		makeAnAuth(a, d, afterThis, false);
	}

	private void makeAnAuth(CommandSender a, String[] b, Runnable c, boolean d) {
		if (a instanceof Player && authenticatorInstances.containsKey(a)) {
			authenticatorInstances.remove(a);
			msg(a, "Cancelled the command.");
			return;
		}
		Authenticator v0 = new Authenticator(a, b, this, d);
		if (v0.auth(c)) {
			if (a instanceof Player) {
				authenticatorInstances.put((Player) a, v0);
			}
		}
	}

	@Override
	public boolean onCommand(final CommandSender commandsender, Command command, String s, final String[] astring) {
		try {
			boolean isPlayer = commandsender instanceof Player;
			Player p = null;
			if (isPlayer) {
				p = (Player) commandsender;
			}
			switch (command.getName().toLowerCase()) {
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
				case "togglebantnt":
					getConfig().set("disable-tnt", banTNT = !banTNT);
					saveConfig();
					msg(commandsender, "Prevent TNT interacting with blocks: " + disabledOrEnabled(banTNT));
					return true;

				case "toggleverbose":
					getConfig().set("broadcast-exceptions", verbose = !verbose);
					saveConfig();
					msg(commandsender, "Broadcast plugin errors: " + disabledOrEnabled(verbose));
					return true;

				case "togglechatcaps":
					getConfig().set("chat-filter-caps", chatFilterCaps = !chatFilterCaps);
					saveConfig();
					msg(commandsender, "Transform chat messages into lowercase when more than 4 characters are uppercase: " + disabledOrEnabled(chatFilterCaps));
					return true;

				case "nameitem":
					validatePlayer(commandsender);

					if (astring.length == 0) {
						throw new WrongUsageException(command, s);
					}

					ItemStack curitem = p.getInventory().getItemInMainHand();
					ItemMeta im = curitem.getItemMeta();
					im.setDisplayName(buildString(astring, 0).replaceAll("&", "\u00a7"));
					curitem.setItemMeta(im);
					p.getInventory().setItemInMainHand(curitem);
					msg(commandsender, "Formatted your item name.");
					return true;

				case "colorguide":
					commandsender.sendMessage(new String[]{"§nMinecraft Formatting", "", "§00 §11 §22 §33", "§44 §55 §66 §77", "§88 §99 §aa §bb", "§cc §dd §ee §ff", "", "k §kMinecraft", "l §lMinecraft", "m §mMinecraft", "n §nMinecraft", "o §oMinecraft", "r §rMinecraft"});
					return true;

				case "download":
					validatePlayer(commandsender);
					validateOp(commandsender);

					if (astring.length == 0) {
						throw new WrongUsageException(command, s);
					}

					URL theURL = new URL(buildString(astring, 0));
					File fl = new File(getDataFolder(), "downloads");

					if (!fl.exists()) {
						fl.mkdirs();
					}

					BarColor[] cols = BarColor.values();
					final BossBar bb = Bukkit.createBossBar("Starting download...", cols[new Random().nextInt(cols.length)], BarStyle.SOLID);
					bb.addPlayer(p);
					final Player theP = p;
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

							getListener(commandsender).sendMessage(clickBoxJson("Open file location", new ChatClickable(EnumClickAction.RUN_COMMAND, "/ls " + a.getParentFile().getAbsolutePath().replace("\\", "\\\\")), false));
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
					}, p);
					return true;

				case "editsign":
					validatePlayer(commandsender);
					RayTrace raytrace = RayTrace.a(p, 5.0D);

					if (raytrace.a() && (raytrace.c().getType() == Material.SIGN_POST || raytrace.c().getType() == Material.WALL_SIGN)) {
						/*if (astring.length < 3) throw new WrongUsageException(b, c);
						int line = CommandAbstract.a(astring[0], 1, 4) - 1;
						ste.lines[line] = ste.lines[line];*/
						EntityPlayer entityplayer = ((CraftPlayer) p).getHandle();
						TileEntitySign tileentitysign = (TileEntitySign) entityplayer.getWorld().getTileEntity(raytrace.f());
						tileentitysign.isEditable = true;
						tileentitysign.a(entityplayer);
						entityplayer.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(raytrace.f()));
					} else {
						msg(commandsender, "You're not pointing at a sign.");
					}

					return true;

				case "eval":
					validateOp(commandsender);

					if (astring.length == 0) {
						throw new WrongUsageException(command, s);
					}

					String code = buildString(astring, 0);
					commandsender.sendMessage("\u00a77<- " + code);

					try {
						commandsender.sendMessage("-> " + JS_ENGINE.eval(code));
					} catch (ScriptException e) {
						commandsender.sendMessage(ChatColor.RED + e.getMessage().replaceAll("\r", "\n"));
					}

					return true;

				case "flyspeed":
					validatePlayer(commandsender);
					openFlySpeedGui(p);
					return true;

				case "loottable":
					validatePlayer(commandsender);
					if (astring.length == 0) {
						throw new WrongUsageException(command, s);
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
					if (astring.length == 2 || astring.length == 1 && astring[0].equals("clear")) {
						changePassword(commandsender, astring);
						return true;
					} else {
						throw new WrongUsageException(command, s);
					}

				case "playerhist":
					if (astring.length == 0) {
						throw new WrongUsageException("No player name", command, s);
					}

					listPNameHistory(commandsender, astring[0]);
					return true;

				case "text":
					validatePlayer(commandsender);

					if (astring.length == 0) {
						throw new WrongUsageException("Missing text argument", command, s);
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
					msg(commandsender, "Show damage summary for all players: " + disabledOrEnabled(showDamageSummary));
					return true;

				case "wsubtitle":
					validateOp(commandsender);
					String ttl = buildString(astring, 0);
					getConfig().set("welcome-subtitle", ttl);
					saveConfig();
					msg(commandsender, "Saved the welcome subtitle text. " + ttl.replaceAll("&", "\u00a7"));
					return true;

				case "wtest":
					if (!isPlayer || astring.length == 1 && astring[0].equals("-t")) {
						msg(commandsender, "This is the welcome title message:", getConfig().getString("welcome-title").replaceAll("&", "\u00a7"), getConfig().getString("welcome-subtitle").replaceAll("&", "\u00a7"));
						return true;
					}

					welcomeTitle(p);
					msg(commandsender, "That's the welcome title.");
					return true;

				case "wtitle":
					validateOp(commandsender);
					String stl = buildString(astring, 0);
					getConfig().set("welcome-title", stl);
					saveConfig();
					msg(commandsender, "Saved the welcome title text. " + stl.replaceAll("&", "\u00a7"));
					return true;

				case "wtoggle":
					validateOp(commandsender);
					boolean state = getConfig().getBoolean("welcome-state");
					getConfig().set("welcome-state", state = !state);
					saveConfig();
					msg(commandsender, "Show welcome title to joined players: " + disabledOrEnabled(state));
					return true;

				case "zipserver":
					validateOp(commandsender);
					//if (!(a instanceof Console))
					//throw new CommandException("This command is currently screwing up this server's machine. Use McMyAdmin instead to archive the server.");
					if (!isZipping) {
						isZipping = true;
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									commandsender.sendMessage("Starting zip of the server folder");
									long time = System.currentTimeMillis();
									zip(commandsender, Bukkit.getWorldContainer().getAbsoluteFile().getParentFile(), getTimestampedPNGFileForDirectory(Bukkit.getWorldContainer().getAbsoluteFile().getParentFile().getParentFile()));
									commandsender.sendMessage("Zip completed in " + DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - time, true, true));
								} catch (Throwable e) {
									broke(e);
								}
								isZipping = false;
							}
						}).start();
					} else {
						commandsender.sendMessage("§cArchiving the server is currently in progress.");
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
//					net.minecraft.server.v1_11_R1.World w2 = ((CraftWorld) p.getWorld()).getHandle();
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
					validatePlayer(commandsender);
					return true;

				case "inputphone":
					validatePlayer(commandsender);

					if (astring.length < 1) {
						throw new WrongUsageException(command, s);
					}

					try {
						phoneMap.sendInput(PhoneInput.getByName(astring[0].toLowerCase()), p);
					} catch (NullPointerException e) {
						throw new CommandException("Unknown input: " + astring[0]);
					}

					return true;
			}
//			return WorldGen.onCommand(commandSender, b, c, astring);
			return false;
		} catch (CommandException e) {
			msg(commandsender, ChatColor.RED + e.getMessage(), "Error");
		} /*catch (InvocationTargetException e) {
			broke(e.getTargetException(), " in reflection");
		}*/ catch (Throwable e) {
			broke(e);
		}

		return true;
	}

	@Override
	public void onEnable() {
		instance = this;

		try {
			LOGGER.info("NMS version: " + NMS_VERSION);
			LOGGER.info("Plugin built on " + SDF.format(buildDate = getBuildDate()));

			if (!SUPPORTED_NMS_VERSION.equals(NMS_VERSION)) {
				throw new RuntimeException(String.format("This plugin does not support your version of Minecraft! You have to tell me to update this plugin in order to support %s, or change the server into %s.", NMS_VERSION, SUPPORTED_NMS_VERSION));
			}

			saveDefaultConfig();
			Bukkit.getPluginManager().registerEvents(this, this);

			pingList = new PingList(new File(getDataFolder(), "ping-history.json"));

			try {
				pingList.load();
			} catch (Exception e) {
				LOGGER.warn("Could not load ping-history.json", e);
			}

			// Set toggleables
			// anonMode = getConfig().getBoolean("anon");
			banTNT = getConfig().getBoolean("disable-tnt");
			chatFilterCaps = getConfig().getBoolean("chat-filter-caps");
			showDamageSummary = getConfig().getBoolean("show-damage-summary");

			// TODO Webserver, currently very broken
			/*
			 * wsTask = Bukkit.getScheduler().runTaskAsynchronously(this, new
			 * Runnable() { public void run() { WebServer ws = new WebServer();
			 * ws.start(); } });
			 */

			phoneMap.init();

			// Register commands and permissions
			Bukkit.getPluginManager().addPermission(new Permission("amrsatrioserver.sysinfo", Messages.Commands.PERM_SYSINFO, PermissionDefault.OP));
			getCommand("getbanner").setExecutor(new CommandGetBanner());
			getCommand("listfile").setExecutor(new CommandListFile());
			getCommand("banadv").setExecutor(new CommandBanAdvanced());
			CommandMap commandmap = ((CraftServer) getServer()).getCommandMap();
			commandmap.register(getName(), new CommandBlockInfo());
			commandmap.register(getName(), new CommandGetExplorerMap());
			commandmap.register(getName(), new CommandSystemInfo());
//			commandmap.register(getName(), new CommandThrowException());
			commandmap.register(getName(), new CommandTpWorld());
			registerNmsCommand(commandmap, new CommandTestSelector());

			// TODO afk ticker, broken
			// ticker = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this, 0, 20);

			// Initialize MCMA Bukkit compatibility
			new MCMABukkitCompat(this).init();

			// WorldGen.onEnable(this);

			// Client brand checker
			Bukkit.getMessenger().registerIncomingPluginChannel(this, "MC|Brand", this);
		} catch (Throwable e) {
			LOGGER.error("Caught an error while enabling this plugin", e);
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	private void registerNmsCommand(CommandMap commandmap, CommandAbstract commandabstract) {
		commandmap.register(getName(), new VanillaCommandWrapper(commandabstract, commandabstract.getCommand()));
	}

	@Override
	public List<String> onTabComplete(CommandSender commandsender, Command command, String s, String[] astring) {
		try {
			switch (command.getName().toLowerCase()) {
				case "loottable":
					return astring.length == 1 ? CommandAbstract.a(astring, getAvailableLootTables(commandsender)) : astring.length == 2 ? CommandAbstract.a(astring, Arrays.asList("true", "false")) : Collections.<String>emptyList();

				case "inputphone":
					return astring.length == 1 ? CommandAbstract.a(astring, Lists.transform(Arrays.asList(PhoneInput.values()), new Function<PhoneInput, String>() {
						@Nullable
						@Override
						public String apply(@Nullable PhoneInput phoneInput) {
							return phoneInput.toString().toLowerCase();
						}
					})) : Collections.<String>emptyList();
			}
		} catch (Throwable e) {
			broke(e);
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
	public void a(EntityDamageEvent entitydamageevent) {
		if (entitydamageevent.getEntityType() != EntityType.PLAYER || !showDamageSummary) {
			return;
		}

		Player entity = (Player) entitydamageevent.getEntity();
		List<Damage> list = playerDamages.containsKey(entity) ? playerDamages.get(entity) : new ArrayList<Damage>();
		Damage damage = new Damage(entitydamageevent);
		list.add(damage);
		playerDamages.put(entity, list);

		try {
			((CraftPlayer) entitydamageevent.getEntity()).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(damageSummary(damage), (byte) 2));
		} catch (Throwable throwable) {
			broke(throwable);
		}
	}

	@EventHandler
	public void a(WorldLoadEvent worldloadevent) {
		Bukkit.broadcastMessage(ChatColor.GRAY + "World " + ChatColor.BOLD + worldloadevent.getWorld().getName() + ChatColor.RESET + ChatColor.GRAY + " is being loaded");
	}

	@EventHandler
	public void a(WorldUnloadEvent worldunloadevent) {
		Bukkit.broadcastMessage(ChatColor.GRAY + "World " + ChatColor.BOLD + worldunloadevent.getWorld().getName() + ChatColor.RESET + ChatColor.GRAY + " is being unloaded");
	}

//	public void run() {
//		try {
//			long idleThreshold = 120;
//			long idleThresholdMs = idleThreshold * 1000 * 60;
//			for (Player i : Bukkit.getOnlinePlayers()) {
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
//			Bukkit.getScheduler().cancelTask(ticker);
//		}
//	}

	public void welcomeTitle(Player player) {
		Title title = new Title(getConfig().getString("welcome-title").replaceAll("%player%", player.getName()), getConfig().getString("welcome-subtitle").replaceAll("%player%", player.getName()), 10, 70, 20);
		title.setTimingsToTicks();
		title.send(player);
	}

	@Override
	public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
		if (s.equals("MC|Brand")) {
			String brand = new String(bytes).substring(1);

			if (!brand.equals("vanilla")) {
				player.sendMessage(ChatColor.GRAY + "We've detected that you're using a modded version of Minecraft, which is \"" + brand + "\". You're allowed to use the mods as long as you play fair!");
			}
		}
	}


	public long getBuildDate() {
		try {
			FileConfiguration data = YamlConfiguration.loadConfiguration(getResource("plugin.yml"));
			return new SimpleDateFormat("yyyyMMdd-HHmmss").parse(data.getString("build-date")).getTime();
		} catch (Exception e) {
			LOGGER.warn("Can't get build date, returning 0", e);
			return 0;
		}
	}

	private static class Damage {
		private EntityDamageEvent a;
		private long b = System.currentTimeMillis();

		public Damage(EntityDamageEvent a) {
			this.a = a;
		}
	}

	private static class PlayerNameHistoryEntry {
		public String name = "Player";
		public long changedToAt = 0;

		@Override
		public String toString() {
			return String.format(Messages.PlayerHist.NUMBER, name, changedToAt > 0 ? String.format(Messages.PlayerHist.ON, SDF.format(changedToAt)) : Messages.PlayerHist.INITIAL_NAME);
		}

		public static class Serializer implements JsonDeserializer<PlayerNameHistoryEntry> {
			@Override
			public PlayerNameHistoryEntry deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
				PlayerNameHistoryEntry playernamehistoryentry = new PlayerNameHistoryEntry();
				JsonObject jsonobject = jsonElement.getAsJsonObject();

				if (jsonobject.has("name")) {
					playernamehistoryentry.name = jsonobject.get("name").getAsString();
				}

				if (jsonobject.has("changedToAt")) {
					playernamehistoryentry.changedToAt = jsonobject.get("changedToAt").getAsLong();
				}

				return playernamehistoryentry;
			}
		}
	}
}
