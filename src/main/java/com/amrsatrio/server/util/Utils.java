package com.amrsatrio.server.util;

import com.amrsatrio.server.ServerPlugin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatClickable;
import net.minecraft.server.v1_14_R1.ChatClickable.EnumClickAction;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatHoverable;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.ChatMessageType;
import net.minecraft.server.v1_14_R1.ChatModifier;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.DedicatedServer;
import net.minecraft.server.v1_14_R1.EntityTippedArrow;
import net.minecraft.server.v1_14_R1.EnumChatFormat;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.PacketPlayOutChat;
import net.minecraft.server.v1_14_R1.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_14_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftMinecartCommand;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftTippedArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final ChatComponentText UNKNOWN_COMPONENT = new ChatComponentText("(???)");
	public static final long KB_IN_BYTES = 1024;
	public static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
	public static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
	public static final long TB_IN_BYTES = GB_IN_BYTES * 1024;
	public static final long PB_IN_BYTES = TB_IN_BYTES * 1024;
	public static final int FLAG_SHORTER = 1;
	public static final int FLAG_CALCULATE_ROUNDED = 1 << 1;
	private static final int BUFFER = 32768;
	public static final ChatHoverable CLICK_TO_TP_HOVER = new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, new ChatMessage("chat.coordinates.tooltip"));
	private static final Gson PRETTY_PRINTING_JSON = new GsonBuilder().setPrettyPrinting().create();
	//	private static final String ENABLED_MSG = "\u2611";
//	private static final String DISABLED_MSG = "\u2610";
	private static final String ENABLED_MSG = "Enabled";
	private static final String DISABLED_MSG = "Disabled";

	private Utils() {
	}

	public static void displayFile(Player player, File file) {
		String msgHead = "Preview";
		player.sendMessage("\u00a76\u00a7l--- " + file.getName() + " ---");
		try {
			switch (FilenameUtils.getExtension(file.getName()).toUpperCase()) {
				case "NBT":
				case "DAT":
				case "SCHEMATIC":
					ServerPlugin.msg(player, ChatColor.ITALIC + "Attempting to read this file as NBT", msgHead);
					try {
						FileInputStream nbtfis = new FileInputStream(file);
						NBTTagCompound root = NBTCompressedStreamTools.a(nbtfis);
						nbtfis.close();
						player.sendMessage(root.toString());
					} catch (ZipException e) {
						ServerPlugin.msg(player, ChatColor.ITALIC + "Not an NBT file!", msgHead);
					}
					break;
				case "JSON":
				case "MCMETA":
					ServerPlugin.msg(player, ChatColor.ITALIC + "Attempting to read this file as JSON", msgHead);
					JsonParser parser = new JsonParser();
					FileReader fr = new FileReader(file);
					JsonElement json = parser.parse(fr);
					fr.close();
					player.sendMessage(PRETTY_PRINTING_JSON.toJson(json));
					break;
				case "CONF":
				case "PROPERTIES":
					ServerPlugin.msg(player, ChatColor.ITALIC + "Attempting to read properties file and showing in inventory", msgHead);
					new PropertiesEditor(player, file).show();
					break;
				default:
					ServerPlugin.msg(player, ChatColor.ITALIC + "Attempting to read this file as text", msgHead);
					try (BufferedReader br = new BufferedReader(new FileReader(file))) {
						String cl;
						while ((cl = br.readLine()) != null) {
							player.sendMessage(cl);
						}
						br.close();
					}
					break;
			}
		} catch (Throwable e) {
			broke(e);
		}
	}

	public static void actionBar(Player player, IChatBaseComponent ichatbasecomponent) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ichatbasecomponent, ChatMessageType.GAME_INFO));
	}

	public static void actionBar(Player player, String s) {
		actionBar(player, new ChatComponentText(s));
	}

	public static IChatBaseComponent suggestBoxJson(String a, String b, boolean c) {
		return clickBoxJson(a, new ChatClickable(EnumClickAction.SUGGEST_COMMAND, b), c);
	}

	public static IChatBaseComponent clickBoxJson(String text, ChatClickable clickAction, boolean addSpaceAfter) {
		IChatBaseComponent ichatbasecomponent = new ChatComponentText('[' + text + ']').setChatModifier(new ChatModifier().setChatClickable(clickAction));

		if (addSpaceAfter) {
			ichatbasecomponent.addSibling(new ChatComponentText(" "));
		}

		return ichatbasecomponent;
	}

	public static void beepOnceNormalPitch(CommandSender commandsender) {
		if (!(commandsender instanceof Player)) {
			return;
		}

		Player player = (Player) commandsender;
		player.playSound(player.getLocation(), "minecraft:block.note.pling", 3.0f, 1);
	}

	public static void broke(Throwable throwable) {
		broke(throwable, "");
	}

	public static void broke(Throwable throwable, String s) {
		boolean flag = ServerPlugin.getInstance().bcPluginErrors.get();
		String s1 = "§4§l§ka§4§l>>§r    §c§lOh nose! I've caught an error" + s + "!§r    §4§l<<§ka";

		if (flag) {
			Bukkit.broadcastMessage(s1);
			Bukkit.broadcastMessage(ChatColor.RED.toString() + throwable);
		} else {
			Bukkit.getConsoleSender().sendMessage(s1);
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED.toString() + throwable);
		}

		for (StackTraceElement i : throwable.getStackTrace()) {
			String string1 = i.getClassName();
			String fcl = string1.substring(0, string1.lastIndexOf('.') + 1) + "\u00a7f" + string1.substring(string1.lastIndexOf('.') + 1);
			String cls = "{\"text\":\"" + string1.substring(string1.lastIndexOf('.') + 1) + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"\u00a77" + fcl + "\"}}";
			String str = "\u00a7r" + (i.isNativeMethod() ? "(\u00a7oNative Method\u00a7r)" : i.getFileName() != null && i.getLineNumber() >= 0 ? "(\u00a7b" + i.getFileName() + "\u00a7r:\u00a73" + i.getLineNumber() + "\u00a7r)" : i.getFileName() != null ? "(\u00a7b" + i.getFileName() + "\u00a7r)" : "(\u00a7oUnknown Source\u00a7r)");

			if (flag) {
				bcmJson("[{\"text\":\"\u00a77at \"}," + cls + ",{\"text\":\".\u00a7a" + i.getMethodName() + str + "\"}]");
			}

			Bukkit.getConsoleSender().sendMessage("\u00a77    at " + fcl + "\u00a7r.\u00a7a" + i.getMethodName() + str);
		}

		if (!flag) {
			Bukkit.broadcastMessage(ChatColor.RED + "An error occurred. See server console for details.");
		}

//		Bukkit.broadcastMessage("\u00a77How about reporting this to me? Click here to report (coming soon)!");
	}

	public static ItemStack applyName(ItemStack itemstack, String s) {
		ItemMeta itemmeta = itemstack.getItemMeta();
		itemmeta.setDisplayName(s);
		itemstack.setItemMeta(itemmeta);
		return itemstack;
	}

	public static String freeOf(long available, long total) {
		long used = total - available;
		return String.format("%s free of %s (%d%% used)", formatFileSize(available), formatFileSize(total), (int) ((double) used / (double) total * 100D));
	}

	private static void bcmJson(String string) {
		((CraftServer) Bukkit.getServer()).getHandle().sendAll(new PacketPlayOutChat(ChatSerializer.a(string)));
	}

	public static String buildString(String[] a, int b) {
		StringBuilder stringbuilder = new StringBuilder();

		for (int i = b; i < a.length; ++i) {
			if (i > b) {
				stringbuilder.append(" ");
			}

			String s = a[i];
			stringbuilder.append(s);
		}

		String res = stringbuilder.toString();
		return res.substring(0, res.length() - (res.endsWith(" ") ? 1 : 0));
	}

	public static File createUniqueCopyName(File path, String fileName) {
		File file = getFile(path, fileName);
		if (!file.exists()) {
			return file;
		}
		return createUniqueCopyName(path, FilenameUtils.removeExtension(fileName) + " - Copy." + FilenameUtils.getExtension(fileName));
	}

	//TODO simple argument
	public static String fancyTime(long l, boolean simple) {
		return DurationFormatUtils.formatDurationWords(l, true, true);
	}

	public static int floor(double a) {
		int i = (int) a;
		return a < i ? i - 1 : i;
	}

	public static String formatFileSize(long sizeBytes) {
		final BytesResult res = formatBytes(sizeBytes, 0);
		return String.format("%1$s %2$s", res.value, res.units);
	}

	public static BytesResult formatBytes(long sizeBytes, int flags) {
		final boolean isNegative = sizeBytes < 0;
		float result = isNegative ? -sizeBytes : sizeBytes;
		String suffix = "B";
		long mult = 1;
		if (result > 900) {
			suffix = "KB";
			mult = KB_IN_BYTES;
			result = result / 1024;
		}
		if (result > 900) {
			suffix = "MB";
			mult = MB_IN_BYTES;
			result = result / 1024;
		}
		if (result > 900) {
			suffix = "GB";
			mult = GB_IN_BYTES;
			result = result / 1024;
		}
		if (result > 900) {
			suffix = "TB";
			mult = TB_IN_BYTES;
			result = result / 1024;
		}
		if (result > 900) {
			suffix = "PB";
			mult = PB_IN_BYTES;
			result = result / 1024;
		}
		// Note we calculate the rounded long by ourselves, but still let String.format()
		// compute the rounded value. String.format("%f", 0.1) might not return "0.1" due to
		// floating point errors.
		final int roundFactor;
		final String roundFormat;
		if (mult == 1 || result >= 100) {
			roundFactor = 1;
			roundFormat = "%.0f";
		} else if (result < 1) {
			roundFactor = 100;
			roundFormat = "%.2f";
		} else if (result < 10) {
			if ((flags & FLAG_SHORTER) != 0) {
				roundFactor = 10;
				roundFormat = "%.1f";
			} else {
				roundFactor = 100;
				roundFormat = "%.2f";
			}
		} else { // 10 <= result < 100
			if ((flags & FLAG_SHORTER) != 0) {
				roundFactor = 1;
				roundFormat = "%.0f";
			} else {
				roundFactor = 100;
				roundFormat = "%.2f";
			}
		}
		if (isNegative) {
			result = -result;
		}
		final String roundedString = String.format(roundFormat, result);
		// Note this might overflow if abs(result) >= Long.MAX_VALUE / 100, but that's like 80PB so
		// it's okay (for now)...
		final long roundedBytes = (flags & FLAG_CALCULATE_ROUNDED) == 0 ? 0 : (long) Math.round(result * roundFactor) * mult / roundFactor;
		return new BytesResult(roundedString, suffix, roundedBytes);
	}

	public static String timeStringFromTicks(int t) {
		DecimalFormat decimalFormat = new DecimalFormat("########0.00");
		double d0 = (double) t / 20.0D;
		double d1 = d0 / 60.0D;
		double d2 = d1 / 60.0D;
		double d3 = d2 / 24.0D;
		double d4 = d3 / 365.0D;
		return d4 > 0.5D ? decimalFormat.format(d4) + " y" : d3 > 0.5D ? decimalFormat.format(d3) + " d" : d2 > 0.5D ? decimalFormat.format(d2) + " h" : d1 > 0.5D ? decimalFormat.format(d1) + " m" : d0 + " s";
	}

//	public static String getIPAddress(boolean useIPv4) {
//		try {
//			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
//			for (NetworkInterface intf : interfaces) {
//				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
//				for (InetAddress addr : addrs) {
//					if (!addr.isLoopbackAddress()) {
//						String sAddr = addr.getHostAddress();
//						// boolean isIPv4 =
//						// InetAddressUtils.isIPv4Address(sAddr);
//						boolean isIPv4 = sAddr.indexOf(':') < 0;
//
//						if (useIPv4) {
//							if (isIPv4) {
//								return sAddr;
//							}
//						} else {
//							if (!isIPv4) {
//								int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
//								return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
//							}
//						}
//					}
//				}
//			}
//		} catch (Throwable ex) {
//		} // for now eat exceptions
//		return "";
//	}

	public static String disabledOrEnabled(boolean flag) {
		return flag ? ChatColor.GREEN + ENABLED_MSG : ChatColor.RED + DISABLED_MSG;
	}

	public static IChatBaseComponent disabledOrEnabledComponent(boolean enabled) {
		return new ChatComponentText(enabled ? ENABLED_MSG : DISABLED_MSG).a(enabled ? EnumChatFormat.GREEN : EnumChatFormat.RED);
	}

	public static File getTimestampedPNGFileForDirectory(File gameDirectory) {
		String s = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
		int i = 1;

		while (true) {
			File file1 = new File(gameDirectory, "MinecraftBackup-" + s + (i == 1 ? "" : "_" + i) + ".zip");

			if (!file1.exists()) {
				return file1;
			}

			++i;
		}
	}

	public static void updateHF(Player a) {
		sendPlayerListText(a, "\u00a7aWelcome to\n\u00a7l" + Bukkit.getName(), "\u00a76You're currently in\n\u00a7l" + a.getWorld().getName());
	}

//	public static String plural(int a) {
//		return a == 1 ? "" : "s";
//	}

//	public static String formatFileSize(long number) {
//		return formatFileSize(number, false);
//	}
//
//	private static String formatFileSize(long number, boolean shorter) {
//		float result = number;
//		String suffix = "B";
//		if (result > 900) {
//			suffix = "KB";
//			result /= 1024;
//		}
//		if (result > 900) {
//			suffix = "MB";
//			result /= 1024;
//		}
//		if (result > 900) {
//			suffix = "GB";
//			result /= 1024;
//		}
//		if (result > 900) {
//			suffix = "TB";
//			result /= 1024;
//		}
//		if (result > 900) {
//			suffix = "PB";
//			result /= 1024;
//		}
//		if (result > 900) {
//			suffix = "EB";
//			result /= 1024;
//		}
//		String value;
//		if (result < 1) value = String.format("%.2f", result);
//		else if (result < 10) {
//			if (shorter) value = String.format("%.1f", result);
//			else value = String.format("%.2f", result);
//		} else if (result < 100) {
//			if (shorter) value = String.format("%.0f", result);
//			else value = String.format("%.2f", result);
//		} else value = String.format("%.0f", result);
//		return value + suffix;
//	}
//
//	public static String formatShortFileSize(long number) {
//		return formatFileSize(number, true);
//	}

//	public static File getCopyFile(File f) {
//		// if (!f.isFile()) throw new IllegalArgumentException("The file is a
//		// folder!");
//		if (f.exists()) {
//			return new File(f.getParentFile(), "Copy of " + f.getName());
//		} else {
//			return f;
//		}
//	}

	public static List<String> getExistingWorlds() {
		List<String> list = new ArrayList<>();

		for (File file : Bukkit.getWorldContainer().listFiles()) {
			if (file.isDirectory() && !file.getName().endsWith("_nether") && !file.getName().endsWith("_the_end")) {
				if (Arrays.asList(file.list()).contains("level.dat")) {
					list.add(file.getName());
				}
			}
		}

		list.sort(String::compareToIgnoreCase);
		return Collections.unmodifiableList(list);
	}

	public static File getFile(File curdir, String file) {
		return getFile(curdir.getAbsolutePath(), file);
	}

	private static File getFile(String curdir, String file) {
		String separator = "/";

		if (curdir.endsWith("/")) {
			separator = "";
		}

		return new File(curdir + separator + file);
	}

//	@Deprecated
//	public static Object getHandle(Object obj) {
//		try {
//			Method method = obj.getClass().getDeclaredMethod("getHandle");
//			method.setAccessible(true);
//			return method.invoke(obj);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return null;
//		}
//	}

//	public static String getVersion() {
//		String name = Bukkit.getServer().getClass().getPackage().getName();
//		return name.substring(name.lastIndexOf('.') + 1) + ".";
//	}
//
//	public static String getVersion2() {
//		String name = Bukkit.getServer().getClass().getPackage().getName();
//		return name.substring(name.lastIndexOf('.') + 1);
//	}

	public static String joinNiceString(Object[] a) {
		return joinNiceString(a, "and");
	}

	public static String joinNiceString(Object[] a, String s) {
		StringBuilder stringbuilder = new StringBuilder();

		for (int i = 0; i < a.length; ++i) {
			String s1 = a[i].toString();

			if (i > 0) {
				if (i == a.length - 1) {
					stringbuilder.append(a.length > 2 ? "," : "").append(" ").append(s).append(" ");
				} else {
					stringbuilder.append(", ");
				}
			}

			stringbuilder.append(s1);
		}

		return stringbuilder.toString();
	}

	public static String readUrl(String urlString) throws IOException {
		URL url = new URL(urlString);

		try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			StringBuilder stringbuilder = new StringBuilder();
			int i;
			char[] achar = new char[BUFFER];

			while ((i = bufferedreader.read(achar)) != -1) {
				stringbuilder.append(achar, 0, i);
			}

			return stringbuilder.toString();
		}
	}

	public static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest messagedigest = MessageDigest.getInstance("SHA1");
		byte[] abyte = messagedigest.digest(input.getBytes());
		StringBuilder stringbuilder = new StringBuilder();

		for (byte anAbyte : abyte) {
			stringbuilder.append(Integer.toString((anAbyte & 0xff) + 0x100, 16).substring(1));
		}

		return stringbuilder.toString();
	}

	public static void sendPlayerListText(Player player, String headerText, String footerText) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(headerFooterPacket(headerText, footerText));
	}

	public static PacketPlayOutPlayerListHeaderFooter headerFooterPacket(String headerText, String footerText) {
		return headerFooterPacket(new ChatComponentText(headerText), new ChatComponentText(footerText));
	}

	public static PacketPlayOutPlayerListHeaderFooter headerFooterPacket(IChatBaseComponent headerText, IChatBaseComponent footerText) {
		PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();
		packet.header = headerText;
		packet.footer = footerText;
		return packet;
	}

//	public static void textSlide(final Player player, String s, final int i, int j, BukkitScheduler scheduler, Plugin plugin) {
//		// String text, int textLengthInFrame, int speedTicks
//		s = ChatColor.stripColor(s);
//		String s1 = "";
//
//		for (int k = -i; k < -1; k++) {
//			s1 += " ";
//		}
//
//		s1 += s;
//
//		for (int k = 0; k < i; k++) {
//			s1 += " ";
//		}
//
//		final String v1 = s1;
//
//		for (int k = 0; k + k <= v1.length(); k++) {
//			final int i1 = k;
//			scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
//				@Override
//				public void run() {
//					actionBar(player, v1.substring(i1, i + i1));
//				}
//			}, j * k);
//		}
//	}

	/**
	 * Copied from {@link org.bukkit.craftbukkit.v1_14_R1.command.VanillaCommandWrapper#getListener(CommandSender)}
	 */
	public static CommandListenerWrapper getListener(CommandSender sender) {
		if (sender instanceof Player) {
			return ((CraftPlayer) sender).getHandle().getCommandListener();
		} else if (sender instanceof BlockCommandSender) {
			return ((CraftBlockCommandSender) sender).getWrapper();
		} else if (sender instanceof CommandMinecart) {
			return ((CraftMinecartCommand) sender).getHandle().getCommandBlock().getWrapper();
		} else if (sender instanceof RemoteConsoleCommandSender) {
			return ((DedicatedServer) MinecraftServer.getServer()).remoteControlCommandListener.f();
		} else if (sender instanceof ConsoleCommandSender) {
			return ((CraftServer) sender.getServer()).getServer().getServerCommandListener();
		} else if (sender instanceof ProxiedCommandSender) {
			return ((ProxiedNativeCommandSender) sender).getHandle();
		} else {
			throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command listener");
		}
	}

	public static void tripleBeepSamePitch(CommandSender commandsender, Plugin plugin) {
		if (!(commandsender instanceof Player)) {
			return;
		}

		final Player asPlayer = (Player) commandsender;
		Runnable sound = () -> asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0F, 0.7F);
		sound.run();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, sound, 4L);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, sound, 8L);
	}

	public static void extractZip(File zipFile, File destDir) throws IOException {
		if (!destDir.exists()) {
			destDir.mkdir();
		}

		try (ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry zipentry = zipinputstream.getNextEntry();

			while (zipentry != null) {
				String s = destDir.getPath() + File.separator + zipentry.getName();
				File file = new File(s);

				if (!zipentry.isDirectory()) {
					file.getParentFile().mkdirs();
					file.createNewFile();

					try (BufferedOutputStream bufferedoutputstream = new BufferedOutputStream(new FileOutputStream(file))) {
						byte[] abyte = new byte[BUFFER];
						int i;

						while ((i = zipinputstream.read(abyte)) != -1) {
							bufferedoutputstream.write(abyte, 0, i);
						}
					}
				} else {
					file.mkdir();
				}

				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();
			}
		}
	}

	public static boolean isInSubDirectory(File dir, File file) {
		return !(file == null || file.isFile()) && (file.equals(dir) || isInSubDirectory(dir, file.getParentFile()));
	}

	// http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
	public static void zip(CommandSender a, File srcFile, File destination) throws IOException {
		if (!FilenameUtils.getExtension(destination.getName()).equals("zip")) {
			throw new IOException("Argument file is not a file with a .ZIP extension! The file name is " + destination.getName());
		}
		if (isInSubDirectory(srcFile, destination)) {
			throw new IOException("Destination is subfolder of source");
		}
		URI base = srcFile.toURI();
		Deque<File> queue = new LinkedList<>();
		queue.push(srcFile);
		OutputStream out = new FileOutputStream(destination);
		Closeable res = out;
		try {
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				srcFile = queue.pop();
				for (File kid : srcFile.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						LOGGER.info("Adding folder: " + name);
						zout.putNextEntry(new ZipEntry(name));
					} else {
						LOGGER.info("Adding file: " + name);
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
		} finally {
			res.close();
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[BUFFER];

		while (true) {
			int readCount = in.read(buffer);

			if (readCount < 0) {
				break;
			}

			out.write(buffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			copy(in, out);
		}
	}

	private static void copy(InputStream in, File file) throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			copy(in, out);
		}
	}

	public static String getRefuseMessage(Result result) {
		switch (result) {
			case ALLOWED: // very silly
				return "that player is allowed to log in";

			case KICK_FULL:
				return "this server is full";

			case KICK_BANNED:
				return "that player is banned";

			case KICK_WHITELIST:
				return "that player is out of the whitelist";

			default:
			case KICK_OTHER:
				return "of an unknown reason";
		}
	}

	public static void printListNumbered(CommandSender commandSender, List<?> list) {
		for (int i = 0; i < list.size(); i++) {
			commandSender.sendMessage(ChatColor.GOLD.toString() + (i + 1) + ". " + ChatColor.RESET + list.get(i));
		}
	}

	public static String ordinal(int i) {
		int mod100 = i % 100;
		int mod10 = i % 10;

		if (mod10 == 1 && mod100 != 11) {
			return i + "st";
		} else if (mod10 == 2 && mod100 != 12) {
			return i + "nd";
		} else if (mod10 == 3 && mod100 != 13) {
			return i + "rd";
		} else {
			return i + "th";
		}
	}

	public static long getEndOfDay(long date) {
		return DateUtils.addMilliseconds(DateUtils.ceiling(new Date(date), Calendar.DATE), -1).getTime();
	}

	public static long getStartOfDay(long date) {
		return DateUtils.truncate(new Date(date), Calendar.DATE).getTime();
	}

	public static IChatBaseComponent ddComponent(String title, IChatBaseComponent description) {
		IChatBaseComponent root = new ChatComponentText("");
		root.addSibling(new ChatComponentText(title + ": ").a(EnumChatFormat.GRAY));

		if (description != null) {
			root.addSibling(description);
		}

		return root;
	}

	public static net.minecraft.server.v1_14_R1.ItemStack getTippedArrowItem(CraftTippedArrow tippedarrow) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		// TODO This is NMS reflection
		EntityTippedArrow nms = tippedarrow.getHandle();
		Method method = EntityTippedArrow.class.getDeclaredMethod("getItemStack");
		method.setAccessible(true);
		return (net.minecraft.server.v1_14_R1.ItemStack) method.invoke(nms);
	}

	public static IChatBaseComponent worldToTextComponent(World nmsWorld) {
		if (nmsWorld == null) {
			return UNKNOWN_COMPONENT;
		}

		return worldToTextComponent(nmsWorld.getWorldData().getName(), ((WorldServer) nmsWorld).getDataManager().getDirectory());
	}

	public static IChatBaseComponent worldToTextComponent(org.bukkit.World bkWorld) {
		if (bkWorld == null) {
			return UNKNOWN_COMPONENT;
		}

		return worldToTextComponent(bkWorld.getName(), bkWorld.getWorldFolder());
	}

	public static IChatBaseComponent worldToTextComponent(String worldName, File worldDir) {
		IChatBaseComponent returned = new ChatComponentText(worldName);
		returned.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, new ChatComponentText("Folder name: " + worldDir.getName())));
		return returned;
	}

	public static IChatBaseComponent blockPositionToComponent(BlockPosition position, boolean omitY) {
		ChatComponentText component = position == null ? UNKNOWN_COMPONENT : new ChatComponentText("[" + position.getX() + (omitY ? "" : ", " + position.getY()) + ", " + position.getZ() + "]");

		if (position != null) {
			component.getChatModifier().setColor(EnumChatFormat.GREEN).setChatHoverable(CLICK_TO_TP_HOVER).setChatClickable(new ChatClickable(EnumClickAction.SUGGEST_COMMAND, String.format("/tp %s %s %s", position.getX() + 0.5D, omitY ? "~" : position.getY() + 0.5D, position.getZ() + 0.5D)));
		}

		return component;
	}

	public static void checkNullity(Object toCheck, String descriptionToAdd, List<String> listOfNullities) {
		if (toCheck == null) {
			listOfNullities.add(descriptionToAdd);
		}
	}

	public static IChatBaseComponent title(IChatBaseComponent text) {
		return new ChatMessage("\n--- %s ---", text.a(EnumChatFormat.GREEN)).a(EnumChatFormat.DARK_GREEN);
	}

	private static class BytesResult {
		public final String value;
		public final String units;
		public final long roundedBytes;

		public BytesResult(String value, String units, long roundedBytes) {
			this.value = value;
			this.units = units;
			this.roundedBytes = roundedBytes;
		}
	}
}