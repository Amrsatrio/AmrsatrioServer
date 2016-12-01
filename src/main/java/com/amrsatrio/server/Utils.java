package com.amrsatrio.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.server.v1_11_R1.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_11_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Utils {
	public static final long KB_IN_BYTES = 1024;
	public static final long MB_IN_BYTES = KB_IN_BYTES * 1024;
	public static final long GB_IN_BYTES = MB_IN_BYTES * 1024;
	public static final long TB_IN_BYTES = GB_IN_BYTES * 1024;
	public static final long PB_IN_BYTES = TB_IN_BYTES * 1024;
	public static final int FLAG_SHORTER = 1 << 0;
	public static final int FLAG_CALCULATE_ROUNDED = 1 << 1;
	private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();
	private static int BUFFER = 32768;

	public static void a(Player a, File b) {
		String msgHead = "Preview";
		a.sendMessage("\u00a76\u00a7l--- " + b.getName() + " ---");
		try {
			switch (FilenameUtils.getExtension(b.getName()).toUpperCase()) {
				case "NBT":
				case "DAT":
				case "SCHEMATIC":
					AmrsatrioServer.msg(a, "\u00a7oAttempting to read this file as NBT", msgHead);
					try {
						FileInputStream nbtfis = new FileInputStream(b);
						NBTTagCompound root = NBTCompressedStreamTools.a(nbtfis);
						nbtfis.close();
						a.sendMessage(root.toString());
					} catch (ZipException e) {
						AmrsatrioServer.msg(a, "\u00a7oNot an NBT file!", msgHead);
					}
					break;
				case "JSON":
				case "MCMETA":
					AmrsatrioServer.msg(a, "\u00a7oAttempting to read this file as JSON", msgHead);
					JsonParser parser = new JsonParser();
					FileReader fr = new FileReader(b);
					JsonElement json = parser.parse(fr);
					fr.close();
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					a.sendMessage(gson.toJson(json));
					break;
				case "CONF":
				case "PROPERTIES":
					AmrsatrioServer.msg(a, "\u00a7oAttempting to read properties file and showing in inventory", msgHead);
					new PropertiesEditor(a, b).show();
					break;
				default:
					AmrsatrioServer.msg(a, "\u00a7oAttempting to read this file as text", msgHead);
					try (BufferedReader br = new BufferedReader(new FileReader(b))) {
						String cl;
						while ((cl = br.readLine()) != null) {
							a.sendMessage(cl);
						}
						br.close();
					}
					break;
			}
		} catch (Throwable e) {
			broke(e);
		}
	}

	@Deprecated
	public static void actionBar(Player a, String b) {
		//TODO This is NMS reflection
		try {
			Class<?> icbc = getNMSClass("IChatBaseComponent");
			Object handle = getHandle(a);
			Object connection = getField(handle.getClass(), "playerConnection").get(handle);
			Method sendPacket = getMethod(connection.getClass(), "sendPacket");
			Object ser = getNMSClass("ChatComponentText").getConstructor(String.class).newInstance(b.replaceAll("&", "\u00a7"));
			Object pkt = getNMSClass("PacketPlayOutChat").getConstructor(icbc, Byte.TYPE).newInstance(ser, (byte) 2);
			sendPacket.invoke(connection, pkt);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void actionBarWithoutReflection(Player player, IChatBaseComponent ichatbasecomponent) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ichatbasecomponent, (byte) 2));
	}

	public static void actionBarWithoutReflection(Player player, String s) {
		actionBarWithoutReflection(player, new ChatComponentText(s));
	}

	public static String actionBoxJson(String a, String b) {
		return actionBoxJson(a, b, false);
	}

	public static String actionBoxJson(String a, String b, boolean c) {
		return clickBoxJson(a, b, "suggest_command", c);
	}

	public static String clickBoxJson(String a, String b, String c) {
		return clickBoxJson(a, b, c, false);
	}

	public static String clickBoxJson(String a, String b, String c, boolean d) {
		return String.format("{\"text\":\"[%s]\",\"clickEvent\":{\"action\":\"%s\",\"value\":\"%s\"}}%s", a, c, b, d ? "{\"text\":\" \"}" : "");
	}

	public static void beepOnceNormalPitch(CommandSender a) {
		if (!(a instanceof Player)) {
			return;
		}

		Player asPlayer = (Player) a;
		asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 1);
	}

	public static void broke(Throwable e) {
		broke(e, "");
	}

	public static void broke(Throwable e, String string) {
		String s = "§4§l§ka§4§l>>§r    §c§lOh nose! I've caught an error" + string + "!§r    §4§l<<§ka";

		if (AmrsatrioServer.verbose) {
			Bukkit.broadcastMessage(s);
			Bukkit.broadcastMessage("\u00a7c" + e.toString());
		} else {
			Bukkit.getConsoleSender().sendMessage(s);
			Bukkit.getConsoleSender().sendMessage("\u00a7c" + e.toString());
		}

		for (StackTraceElement i : e.getStackTrace()) {
			String string1 = i.getClassName();
			String fcl = string1.substring(0, string1.lastIndexOf('.') + 1) + "\u00a7f" + string1.substring(string1.lastIndexOf('.') + 1);
			String cls = "{\"text\":\"" + string1.substring(string1.lastIndexOf('.') + 1) + "\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"\u00a77" + fcl + "\"}}";
			String str = "\u00a7r" + (i.isNativeMethod() ? "(\u00a7oNative Method\u00a7r)" : (i.getFileName() != null && i.getLineNumber() >= 0 ? "(\u00a7b" + i.getFileName() + "\u00a7r:\u00a73" + i.getLineNumber() + "\u00a7r)" : (i.getFileName() != null ? "(\u00a7b" + i.getFileName() + "\u00a7r)" : "(\u00a7oUnknown Source\u00a7r)")));

			if (AmrsatrioServer.verbose) {
				bcmJson("[{\"text\":\"\u00a77at \"}," + cls + ",{\"text\":\".\u00a7a" + i.getMethodName() + str + "\"}]");
			}

			Bukkit.getConsoleSender().sendMessage("\u00a77    at " + fcl + "\u00a7r.\u00a7a" + i.getMethodName() + str);
		}

		if (!AmrsatrioServer.verbose) {
			Bukkit.broadcastMessage(ChatColor.RED + "An error occured. See server console for details.");
		}

//		Bukkit.broadcastMessage("\u00a77How about reporting this to me? Click here to report (coming soon)!");
	}

	public static ItemStack applyName(ItemStack a, String b) {
		ItemMeta im = a.getItemMeta();
		im.setDisplayName(b);
		a.setItemMeta(im);
		return a;
	}

	public static String freeOf(long available, long total) {
		long used = total - available;
		return String.format("%s free of %s (%d%% used)", Utils.formatFileSize(available), Utils.formatFileSize(total), (int) ((double) used / (double) total * 100D));
	}

	private static void bcmJson(String string) {
		try {
			Class<?> icbc = getNMSClass("IChatBaseComponent$ChatSerializer");
			Object ser = icbc.getMethod("a", String.class).invoke(icbc.newInstance(), string);
			Object pkt = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent")).newInstance(ser);
			Object nmsS1 = getHandle(Bukkit.getServer());
			nmsS1.getClass().getMethod("sendAll", getNMSClass("Packet")).invoke(nmsS1, pkt);
			/*
			 * for (Player a : Bukkit.getOnlinePlayers()) { Object handle =
			 * getHandle(a); Object connection = getField(handle.getClass(),
			 * "playerConnection").get(handle); Method sendPacket =
			 * getMethod(connection.getClass(), "sendPacket");
			 * sendPacket.invoke(connection, pkt); }
			 */
		} catch (Throwable e) {
			e.printStackTrace();
		}
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

	public static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
		boolean equal = true;
		if (l1.length != l2.length) {
			return false;
		}
		for (int i = 0; i < l1.length; i++) {
			if (l1[i] != l2[i]) {
				equal = false;
				break;
			}
		}
		return equal;
	}

	public static File createUniqueCopyName(File path, String fileName) {
		File file = getFile(path, fileName);
		if (!file.exists()) {
			return file;
		}
		return createUniqueCopyName(path, FilenameUtils.removeExtension(fileName) + " - Copy." + FilenameUtils.getExtension(fileName));
	}

	public static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
		if (a.length != o.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i])) {
				return false;
			}
		}
		return true;
	}

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
		final boolean isNegative = (sizeBytes < 0);
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
		final long roundedBytes = (flags & FLAG_CALCULATE_ROUNDED) == 0 ? 0 : (((long) Math.round(result * roundFactor)) * mult / roundFactor);
		return new BytesResult(roundedString, suffix, roundedBytes);
	}

	public static String timeStringFromTicks(int t) {
		DecimalFormat decimalFormat = new DecimalFormat("########0.00");
		double d0 = (double) t / 20.0D;
		double d1 = d0 / 60.0D;
		double d2 = d1 / 60.0D;
		double d3 = d2 / 24.0D;
		double d4 = d3 / 365.0D;
		return d4 > 0.5D ? decimalFormat.format(d4) + " y" : (d3 > 0.5D ? decimalFormat.format(d3) + " d" : (d2 > 0.5D ? decimalFormat.format(d2) + " h" : (d1 > 0.5D ? decimalFormat.format(d1) + " m" : d0 + " s")));
	}

	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress();
						// boolean isIPv4 =
						// InetAddressUtils.isIPv4Address(sAddr);
						boolean isIPv4 = sAddr.indexOf(':') < 0;

						if (useIPv4) {
							if (isIPv4) {
								return sAddr;
							}
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
								return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
							}
						}
					}
				}
			}
		} catch (Throwable ex) {
		} // for now eat exceptions
		return "";
	}

	public static String disabledOrEnabled(boolean flag) {
		return flag ? "\u00a7aEnabled" : "\u00a7cDisabled";
	}

	public static File getTimestampedPNGFileForDirectory(File gameDirectory) {
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

	public static void updateHF(Player a) {
		tabHeaderFooter(a, "\u00a7aWelcome to\n\u00a7l" + Bukkit.getServerName(), "\u00a76You're currently in\n\u00a7l" + a.getWorld().getName());
	}

	public static String plural(int a) {
		return a == 1 ? "" : "s";
	}

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

	public static File getCopyFile(File f) {
		// if (!f.isFile()) throw new IllegalArgumentException("The file is a
		// folder!");
		if (f.exists()) {
			return new File(f.getParentFile(), "Copy of " + f.getName());
		} else {
			return f;
		}
	}

	public static List<String> getExistingWorlds() {
		List<String> res = new ArrayList<>();
		for (File i : Bukkit.getWorldContainer().listFiles()) {
			if (i.isDirectory() && !i.getName().endsWith("_nether") && !i.getName().endsWith("_the_end")) {
				for (String j : i.list()) {
					if (j.equals("level.dat")) {
						res.add(i.getName());
						break;
					}
				}
			}
		}
		return res;
	}

	public static Field getField(Class<?> clazz, String name) {
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
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

	@Deprecated
	public static Object getHandle(Object obj) {
		try {
			return getMethod("getHandle", obj.getClass()).invoke(obj);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(name) && (args.length == 0 || ClassListEqual(args, m.getParameterTypes()))) {
				m.setAccessible(true);
				return m;
			}
		}
		return null;
	}

	public static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
		Class<?>[] t = toPrimitiveTypeArray(paramTypes);
		for (Method m : clazz.getMethods()) {
			Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
			if (m.getName().equals(name) && equalsTypeArray(types, t)) {
				return m;
			}
		}
		return null;
	}

	public static Class<?> getNMSClass(String className) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + getVersion() + className);
	}

	public static Class<?> getOBCClass(String className) throws ClassNotFoundException {
		return Class.forName("org.bukkit.craftbukkit." + getVersion() + className);
	}

	public static Class<?> getPrimitiveType(Class<?> clazz) {
		return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES.get(clazz) : clazz;
	}

	public static String getVersion() {
		String name = Bukkit.getServer().getClass().getPackage().getName();
		return name.substring(name.lastIndexOf('.') + 1) + ".";
	}

	public static String getVersion2() {
		String name = Bukkit.getServer().getClass().getPackage().getName();
		return name.substring(name.lastIndexOf('.') + 1);
	}

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

	public static String joinNiceStringFromCollection(Collection<String> strings) {
		return joinNiceString(strings.toArray(new String[strings.size()]));
	}

	public static void jsonMsg(Player a, String b) {
		jsonMsg(a, b, true);
	}

	public static void jsonMsg(Player a, String b, boolean c) {
		try {
			Class<?> icbc = getNMSClass("IChatBaseComponent$ChatSerializer");
			Object handle = getHandle(a);
			Object connection = getField(handle.getClass(), "playerConnection").get(handle);
			Method sendPacket = getMethod(connection.getClass(), "sendPacket");
			Object ser = icbc.getMethod("a", String.class).invoke(icbc.newInstance(), c ? String.format("[{\"text\":\"%s\"},%s]", String.format(AmrsatrioServer.SERVER_HEADER, "Server"), b) : b);
			Object pkt = getNMSClass("PacketPlayOutChat").getConstructor(getNMSClass("IChatBaseComponent")).newInstance(ser);
			sendPacket.invoke(connection, pkt);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static String readUrl(String urlString) throws Exception {
		BufferedReader reader = null;
		try {
			URL url = new URL(urlString);
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			StringBuffer buffer = new StringBuffer();
			int read;
			char[] chars = new char[1024];
			while ((read = reader.read(chars)) != -1) {
				buffer.append(chars, 0, read);
			}
			return buffer.toString();
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static String sha1(String input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static void tabHeaderFooter(Player player, String s, String s1) {
		try {
			PacketPlayOutPlayerListHeaderFooter packetplayoutplayerlistheaderfooter = new PacketPlayOutPlayerListHeaderFooter(new ChatComponentText(s));
			Field field = packetplayoutplayerlistheaderfooter.getClass().getDeclaredField("b");
			field.setAccessible(true);
			field.set(packetplayoutplayerlistheaderfooter, new ChatComponentText(s1));
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetplayoutplayerlistheaderfooter);
		} catch (Throwable e) {
			AmrsatrioServer.LOGGER.warn("Unable to send player list header footer to " + player.getName(), e);
		}
	}

	public static void textSlide(final Player player, String s, final int i, int j, BukkitScheduler bukkitscheduler, JavaPlugin javaplugin) {
		// String text, int textLengthInFrame, int speedTicks
		s = ChatColor.stripColor(s);
		String s1 = "";

		for (int k = -i; k < -1; k++) {
			s1 += " ";
		}

		s1 += s;

		for (int k = 0; k < i; k++) {
			s1 += " ";
		}

		final String v1 = s1;

		for (int k = 0; k + k <= v1.length(); k++) {
			final int i1 = k;
			bukkitscheduler.scheduleSyncDelayedTask(javaplugin, new Runnable() {
				@Override
				public void run() {
					actionBarWithoutReflection(player, v1.substring(i1, i + i1));
				}
			}, j * k);
		}
	}

	public static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
		int a = classes != null ? classes.length : 0;
		Class<?>[] types = new Class<?>[a];
		for (int i = 0; i < a; i++) {
			types[i] = getPrimitiveType(classes[i]);
		}
		return types;
	}

	@SuppressWarnings("deprecation")
	public static ICommandListener getListener(CommandSender sender) {
		if (sender instanceof Player) {
			return ((CraftPlayer) sender).getHandle();
		}
		if (sender instanceof BlockCommandSender) {
			return ((CraftBlockCommandSender) sender).getTileEntity();
		}
//		if (sender instanceof CommandMinecart) {
//			return ((CraftMinecartCommand) sender).getHandle().getCommandBlock();
//		}
		if (sender instanceof RemoteConsoleCommandSender) {
			return ((DedicatedServer) MinecraftServer.getServer()).remoteControlCommandListener;
		}
		if (sender instanceof ConsoleCommandSender) {
			return ((CraftServer) sender.getServer()).getServer();
		}
		if (sender instanceof ProxiedCommandSender) {
			return ((ProxiedNativeCommandSender) sender).getHandle();
		}
		throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command listener");
	}

	public static void tripleBeepSamePitch(CommandSender commandsender, JavaPlugin javaplugin) {
		if (!(commandsender instanceof Player)) {
			return;
		}
		final Player asPlayer = (Player) commandsender;
		Runnable sound = new Runnable() {
			@Override
			public void run() {
				asPlayer.playSound(asPlayer.getLocation(), "minecraft:block.note.pling", 3.0f, 0.5f);
			}
		};
		sound.run();
		Bukkit.getScheduler().scheduleSyncDelayedTask(javaplugin, sound, 4L);
		Bukkit.getScheduler().scheduleSyncDelayedTask(javaplugin, sound, 8L);
	}

	public static void extractZip(File zipFile, File destDir) throws Exception {
		ZipInputStream zis = null;
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		zis = new ZipInputStream(new FileInputStream(zipFile));
		ZipEntry entry = zis.getNextEntry();
		while (entry != null) {
			String filePath = destDir.getPath() + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				new File(filePath).getParentFile().mkdirs();
				new File(filePath).createNewFile();
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
				byte[] bytesIn = new byte[BUFFER];
				int read = 0;
				while ((read = zis.read(bytesIn)) != -1) {
					bos.write(bytesIn, 0, read);
				}
				bos.close();
			} else {
				File dir = new File(filePath);
				dir.mkdir();
			}
			zis.closeEntry();
			entry = zis.getNextEntry();
		}
		zis.close();
	}

	public static boolean isInSubDirectory(File dir, File file) {
		return !(file == null || file.isFile()) && (file.equals(dir) || isInSubDirectory(dir, file.getParentFile()));
	}

	public static void noReflection(CommandSender a) {
		String s = "\u00a7c\u00a7lWarning: this command only work in 1.10!";
		if (a instanceof Player) {
			actionBar((Player) a, s);
		} else {
			AmrsatrioServer.msg(a, s);
		}
	}

	// http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
	public static void zip(CommandSender a, File srcFile, File destination) throws IOException {
		if (!FilenameUtils.getExtension(destination.getName()).equals("zip")) {
			throw new IllegalArgumentException("Argument file is not a file with a .ZIP extension! The file name is " + destination.getName());
		}
		if (Utils.isInSubDirectory(srcFile, destination)) {
			throw new IllegalArgumentException("Destination is subfolder of source");
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
						a.sendMessage("Adding folder: " + name);
						zout.putNextEntry(new ZipEntry(name));
					} else {
						a.sendMessage("Adding file: " + name);
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

	public static String getRefuseMessage(PlayerPreLoginEvent.Result result) {
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

	public static void printListNumbered(CommandSender commandSender, List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			commandSender.sendMessage((i + 1) + "." + list.get(i));
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

	public static class BytesResult {
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