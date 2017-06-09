package com.amrsatrio.server;

import net.minecraft.server.v1_12_R1.ChatClickable;
import net.minecraft.server.v1_12_R1.ChatClickable.EnumClickAction;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.ChatModifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

public class PropertiesEditor {
	private static final String TAG = "PropEdit";
	public boolean waiting = false;
	private Player player;
	private File file;
	private Properties prop;
	private Inventory inv;
	private String currentKey = null;
	private boolean canEdit;

	public PropertiesEditor(Player a, File b) throws IOException {
		player = a;
		file = b;
		prop = new Properties();
		canEdit = Utils.isInSubDirectory(Bukkit.getWorldContainer().getAbsoluteFile().getParentFile(), file.getParentFile()) || file.getParentFile().getName().equals("108.61.184.122:10210");

		try (FileInputStream fileinputstream = new FileInputStream(b)) {
			prop.load(fileinputstream);
		}
	}

	private void a() {
		inv = Bukkit.createInventory(null, (int) Math.ceil((double) prop.size() / 9) * 9, (canEdit ? "" : "\u00a7o") + file.getName() + " - Properties editor");
		int i = 0;

		for (Entry<Object, Object> n : new TreeMap<>(prop).entrySet()) {
			ChatColor col = ChatColor.GRAY;
			ItemStack it = new ItemStack(Material.PAPER);
			ItemMeta im = it.getItemMeta();
			im.setDisplayName(ChatColor.RESET.toString() + n.getKey());
			String val = n.getValue().toString();

			if (val.isEmpty()) {
				val = ChatColor.ITALIC + Messages.PropEdit.NOT_SET;
			}

			if (val.toLowerCase().equals("true")) {
				col = ChatColor.GREEN;
			}

			if (val.toLowerCase().equals("false")) {
				col = ChatColor.RED;
			}

			ArrayList<String> lore = new ArrayList<>();

			for (String j : val.split("(?<=\\G.{35})")) {
				lore.add(col + j);
			}

			im.setLore(lore);
			it.setItemMeta(im);
			inv.setItem(i++, it);
		}
	}

	public void show() {
		a();
		if (player.getOpenInventory() != null) {
			player.closeInventory();
		}
		player.openInventory(inv);
		ServerPlugin.propEditInstances.put(player, this);
		player.sendMessage(canEdit ? Messages.PropEdit.CAN_EDIT : Messages.PropEdit.CANT_EDIT);

		if (canEdit) {
			player.sendMessage(Messages.PropEdit.HELP);
		}
	}

	public void a(AsyncPlayerChatEvent a) {
		if (currentKey != null) {
			a.setCancelled(true);
			prop.setProperty(currentKey, a.getMessage());
			currentKey = null;
			waiting = false;
			show();
		}
	}

	public void a(InventoryClickEvent a) {
		a.setCancelled(true);

		if (currentKey == null && a.getCurrentItem().getItemMeta() != null && a.getClick() == ClickType.LEFT && canEdit) {
			currentKey = ChatColor.stripColor(a.getCurrentItem().getItemMeta().getDisplayName());
			waiting = true;
			player.closeInventory();
			ServerPlugin.msg(player, String.format(Messages.PropEdit.TYPE_VALUE, currentKey), TAG);
			((CraftPlayer) player).getHandle().sendMessage(new ChatComponentText(Messages.PropEdit.CLICK_TO_INSERT).setChatModifier(new ChatModifier().setUnderline(true).setChatClickable(new ChatClickable(EnumClickAction.SUGGEST_COMMAND, prop.getProperty(currentKey, "UNKNOWN")))));
		} else if (a.getClick() == ClickType.SHIFT_RIGHT) {
			player.closeInventory();

			if (!canEdit) {
				ServerPlugin.msg(player, ChatColor.RED + Messages.PropEdit.CANT_EDIT_OUTSIDE_SERVER, TAG);
				return;
			}

			try (FileOutputStream fos = new FileOutputStream(file)) {
				prop.store(fos, null);
				ServerPlugin.msg(player, Messages.PropEdit.SAVE_SUCCESS, TAG);
			} catch (Throwable e) {
				ServerPlugin.LOGGER.warn("Can't save properties file " + file, e);
				ServerPlugin.msg(player, Messages.PropEdit.SAVE_FAILED, TAG);
			}
		}
	}
}
