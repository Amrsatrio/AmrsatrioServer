package com.amrsatrio.server;

import net.minecraft.server.v1_11_R1.ChatClickable;
import net.minecraft.server.v1_11_R1.ChatClickable.EnumClickAction;
import net.minecraft.server.v1_11_R1.ChatComponentText;
import net.minecraft.server.v1_11_R1.ChatModifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
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
	private Player p;
	private File f;
	private Properties prop;
	private Inventory inv;
	private String currentK = null;
	private boolean canEdit;

	public PropertiesEditor(Player a, File b) throws Exception {
		p = a;
		f = b;
		prop = new Properties();
		canEdit = Utils.isInSubDirectory(Bukkit.getWorldContainer().getAbsoluteFile().getParentFile(), f.getParentFile()) || f.getParentFile().getName().equals("108.61.184.122:10210");
		FileInputStream fis = new FileInputStream(b);
		prop.load(fis);
		fis.close();
	}

	private void a() {
		inv = Bukkit.createInventory(null, (int) Math.ceil((double) prop.size() / 9) * 9, (canEdit ? "" : "\u00a7o") +
				f.getName() + " - Properties editor");
		int i = 0;

		for (Entry<Object, Object> n : new TreeMap<>(prop).entrySet()) {
			String col = "7";
			ItemStack it = new ItemStack(Material.PAPER);
			ItemMeta im = it.getItemMeta();
			im.setDisplayName("\u00a7r" + n.getKey());
			String val = n.getValue().toString();
			if (val.isEmpty()) {
				val = "\u00a7o<not set>";
			}
			if (val.toLowerCase().equals("true")) {
				col = "a";
			}
			if (val.toLowerCase().equals("false")) {
				col = "c";
			}
			ArrayList<String> lore = new ArrayList<>();

			for (String j : val.split("(?<=\\G.{35})")) {
				lore.add("\u00a7" + col + j);
			}

			im.setLore(lore);
			it.setItemMeta(im);
			inv.setItem(i++, it);
		}
	}

	public void show() {
		a();
		if (p.getOpenInventory() != null) {
			p.closeInventory();
		}
		p.openInventory(inv);
		AmrsatrioServer.propEditInstances.put(p, this);
		p.sendMessage("You can " + (canEdit ? "edit" : "only read") + " this file.");

		if (canEdit) {
			p.sendMessage("To edit a key, click a paper. To save, press shift+right click. Close the inventory to discard changes.");
		}
	}

	public void a(AsyncPlayerChatEvent a) {
		if (currentK != null) {
			a.setCancelled(true);
			prop.setProperty(currentK, a.getMessage());
			currentK = null;
			waiting = false;
			show();
		}
	}

	public void a(InventoryClickEvent a) {
		a.setCancelled(true);

		if (currentK == null && a.getCurrentItem().getItemMeta() != null && a.getClick() == ClickType.LEFT && canEdit) {
			currentK = ChatColor.stripColor(a.getCurrentItem().getItemMeta().getDisplayName());
			waiting = true;
			p.closeInventory();
			AmrsatrioServer.msg(p, "Type the desired value for " + currentK + " in the chat box.", "PropEdit");
			((CraftPlayer) p).getHandle().sendMessage(new ChatComponentText("Click me to insert current value into chat box").setChatModifier(new ChatModifier().setUnderline(true).setChatClickable(new ChatClickable(EnumClickAction.SUGGEST_COMMAND, prop.getProperty(currentK, "UNKNOWN")))));
		} else if (a.getClick() == ClickType.SHIFT_RIGHT) {
			p.closeInventory();

			if (!canEdit) {
				AmrsatrioServer.msg(p, "\u00a7cFor security reasons, you cannot modify files outside this server's folder.", TAG);
				return;
			}

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(f);
				prop.store(fos, null);
				AmrsatrioServer.msg(p, "Successfully saved the file.", TAG);
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
