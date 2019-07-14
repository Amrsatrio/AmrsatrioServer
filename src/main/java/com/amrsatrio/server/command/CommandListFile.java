package com.amrsatrio.server.command;

import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.util.Utils;
import net.minecraft.server.v1_14_R1.ChatClickable;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.EnumChatFormat;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CommandListFile implements CommandExecutor {
	public static HashMap<Player, FileGui> fileGuis = new HashMap<>();
	public static String nameForParent = "..";

	public static String checkMark(boolean a) {
		return a ? "\u00a7a\u2713" : "\u00a7c\u2717";
	}

	private static ItemStack getItemForFile(File f) throws IOException {
		ItemStack itemstack = new ItemStack(f.isDirectory() ? f.getName().equals(nameForParent) ? Material.NETHER_STAR : Material.BOOK : Material.PAPER);
		ItemMeta im = itemstack.getItemMeta();
		im.setDisplayName("\u00a7" + (f.isHidden() ? "7" : "r") + f.getName());
		String clickedDir = f.getName();
		int dotIndex = clickedDir.lastIndexOf('.');
		String ext = dotIndex == -1 ? "" : clickedDir.substring(dotIndex + 1).toUpperCase();
		List<String> theArray = new ArrayList<>();
		if (f.getName().equals(nameForParent)) {
			theArray = Collections.singletonList("Up/parent folder");
		} else {
			theArray.add("");
			if (f.canRead() && f.isDirectory() && f.list() != null) {
				int items = f.list().length;
				itemstack.setAmount(items > 127 ? 127 : items);
				theArray.add("\u00a7r" + items + " item" + (items == 1 ? "" : "s"));
			}
			BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
			theArray.add("\u00a7rCreated: " + new SimpleDateFormat().format(new Date(attrs.creationTime().toMillis())));
			theArray.add("\u00a7rModified: " + new SimpleDateFormat().format(new Date(f.lastModified())));
			theArray.add("\u00a7rAccessed: " + new SimpleDateFormat().format(new Date(attrs.lastAccessTime().toMillis())));
			if (f.isFile()) {
				theArray.add("\u00a7rSize: " + Utils.formatFileSize(f.length()));
				theArray.add("\u00a7rType: " + ext);
			}
			theArray.add("\u00a7rPermissions:");
			theArray.add("\u00a7r " + checkMark(f.canRead()) + " Read");
			theArray.add("\u00a7r " + checkMark(f.canWrite()) + " Write");
			theArray.add("\u00a7r " + checkMark(f.canExecute()) + " Execute");
		}
		im.setLore(theArray);
		itemstack.setItemMeta(im);
		return itemstack;
	}

	@Override
	public boolean onCommand(CommandSender a, Command command, String label, String[] b) {
		try {
			if (!(a instanceof Player)) {
				throw new CommandException("Not a player");
			}
			Player v0 = (Player) a;
			File f = new File(Utils.buildString(b, 0));
			if (b.length == 0) {
				f = new File(System.getProperty("user.dir"));
			}
			if (!f.exists()) {
				throw new CommandException("Non-existent folder: " + f.getPath());
			}
			if (f.listFiles() == null) {
				throw new CommandException("Unable to list folder contents, no permission");
			}
			if (f.listFiles().length == 0) {
				throw new CommandException("Empty folder");
			}
			//System.out.println(currentFile);
			FileGui fg = new FileGui(v0, f);
			fileGuis.put(v0, fg);
			fg.refreshItems();
			return true;
		} catch (CommandException e) {
			ServerPlugin.msg(a, "\u00a7c" + e.getMessage());
			return true;
		}
	}

	public class FileGui {
		private final List<String> allowedExts = Arrays.asList("CONF", "CFG", "TXT", "LOG", "JSON", "PROPERTIES", "YML", "INI", "DAT", "NBT", "BAT", "SCHEMATIC");
		public boolean switching = false;
		private Player player;
		private File currentFile;
		private int pages;
		private int page = 1;
		private ArrayList<File> entries = new ArrayList<>();
		private int PREV_SLOT = 45;
		private int NEXT_SLOT = 53;

		public FileGui(Player a, File b) {
			//System.out.println("Creating filegui instance: " + b);
			player = a;
			currentFile = b;
			ArrayList<File> folders = new ArrayList<>();
			ArrayList<File> files = new ArrayList<>();

			if (currentFile.getParentFile() != null) {
				files.add(new File(currentFile, nameForParent));
			}

			for (File i : currentFile.listFiles()) {
				ArrayList<File> toAdd = i.isDirectory() ? folders : files;
				toAdd.add(i);
			}

			Collections.sort(files);
			Collections.sort(folders);
			entries.addAll(folders);
			entries.addAll(files);
			pages = (int) Math.ceil((double) entries.size() / 45d);
		}

		public void handle(InventoryClickEvent a) {
			if (a.getCurrentItem().getItemMeta() == null) {
				return;
			}

			a.setCancelled(true);

			if (a.getSlot() == PREV_SLOT) {
				page--;
				refreshItems();
			} else if (a.getSlot() == NEXT_SLOT) {
				page++;
				refreshItems();
			} else {
				if (a.getClick() != ClickType.DOUBLE_CLICK) {
					return;
				}

				String clickedDir = ChatColor.stripColor(a.getCurrentItem().getItemMeta().getDisplayName());
				final String thef = new File(currentFile, clickedDir).getAbsolutePath();

				if (new File(thef).isDirectory()) {
					String thef2 = thef;
					File pf = currentFile.getParentFile();

					if (clickedDir.equals(nameForParent) && pf != null) {
						thef2 = pf.getAbsolutePath();
					}

					//CommandListFile.fileGuis.remove(player);
					Bukkit.dispatchCommand(player, "listfile " + thef2);
					return;
				} else if (allowedExts.contains(FilenameUtils.getExtension(clickedDir).toUpperCase())) {
					new Thread(() -> Utils.displayFile(player, new File(thef))).start();
					return;
				}
			}

			refreshItems();
		}

		public void handleClose(InventoryCloseEvent a) {
			if (switching) {
				return;
			}

			String path = currentFile.getAbsolutePath();//.replace("\\", "\\\\");
			IChatBaseComponent goToMsg = new ChatComponentText("Go to " + path).a(EnumChatFormat.ITALIC);
			goToMsg.getChatModifier().setChatClickable(new ChatClickable(ChatClickable.EnumClickAction.RUN_COMMAND, "/ls	 " + path));
			((CraftPlayer) player).getHandle().sendMessage(goToMsg);

			if (!switching) {
				fileGuis.remove(player);
			}
		}

		private void refreshItems() {
			try {
				//System.out.println(entries);
				//System.out.println("Refreshing " + currentFile);
				Map<Integer, ItemStack> cont = new HashMap<>();
				int i = 0;

				for (int var0 = (page - 1) * 5 * 9; var0 < entries.size(); var0++) {
					ItemStack itemstack = getItemForFile(entries.get(var0));
					cont.put(i++, itemstack);

					if (i >= 5 * 9) {
						break;
					}

					if (page - 1 > 0) {
						ItemStack back = new ItemStack(Material.ARROW);
						Utils.applyName(back, "< Page " + (page - 1));
						cont.put(PREV_SLOT, back);
					}

					if (page + 1 <= pages) {
						ItemStack next = new ItemStack(Material.ARROW);
						Utils.applyName(next, "Page " + (page + 1) + " >");
						cont.put(NEXT_SLOT, next);
					}
				}

				show(cont);
			} catch (Throwable e) {
				Utils.broke(e);
			}
			//System.out.println(cont);
		}

		public void show(Map<Integer, ItemStack> cont) {//(int) Math.ceil((double) entries.size() / 9)
			Inventory inv = Bukkit.createInventory(null, 6 * 9, (currentFile.getName().isEmpty() ? "Root" : currentFile.getName()) + (pages > 1 ? String.format(" (%d/%d)", page, pages) : ""));
			//System.out.println("Opening " + currentFile + ": " + inv.getName());

			for (Entry<Integer, ItemStack> i : cont.entrySet()) {
				inv.setItem(i.getKey(), i.getValue());
			}

			switching = true;
			// player.closeInventory();
			player.openInventory(inv);
			switching = false;
		}
	}
}
