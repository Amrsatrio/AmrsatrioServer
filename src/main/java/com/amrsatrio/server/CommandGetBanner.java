package com.amrsatrio.server;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandGetBanner implements CommandExecutor {
	public static HashMap<Player, GetBannerGui> openGetBanners = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean isPlayer = sender instanceof Player;
		if (!isPlayer) return true;
		Player p = (Player) sender;
		GetBannerGui gbg = new GetBannerGui(p);
		gbg.refreshItems();
		openGetBanners.put(p, gbg);
		return true;
	}

	@SuppressWarnings("deprecation")
	static class GetBannerGui {
		private Player pl;
		private boolean color = true;
		private boolean first = true;
		private ItemStack banner = new ItemStack(Material.BANNER);
		private PatternType toAddPattern;
		public boolean switching = false;

		public GetBannerGui(Player a) {
			this.pl = a;
		}

		public void show(Map<Integer, ItemStack> cont) {
			Inventory inv = Bukkit.createInventory(pl, 6 * 9,
					color ? (first ? "Select Base Color" : "Select Layer Color") : "Add Layer");
			for (Map.Entry<Integer, ItemStack> i : cont.entrySet()) {
				inv.setItem(i.getKey(), i.getValue());
			}
			switching = true;
			// pl.closeInventory();
			pl.openInventory(inv);
			switching = false;
		}

		public void handle(InventoryClickEvent a) throws Exception {
			if (a.getCurrentItem() == null || a.getCurrentItem().getItemMeta() == null) return;
			a.setCancelled(true);
			if (color) {
				DyeColor c = DyeColor.getByDyeData(a.getCurrentItem().getData().getData());
				BannerMeta bm = (BannerMeta) banner.getItemMeta();
				if (first) {
					bm.setBaseColor(c);
					first = false;
				} else {
					bm.addPattern(new Pattern(c, toAddPattern));
				}
				banner.setItemMeta(bm);
				color = false;
			} else if (!color) {
				if (a.getSlot() == 4) {
					pl.closeInventory();
					Class<?> is = Utils.getNMSClass("ItemStack");
					Object entityplayer = Utils.getHandle(pl);
					Object entityitem = entityplayer.getClass().getMethod("drop", is, Boolean.TYPE).invoke(entityplayer,
							Utils.getOBCClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class)
									.invoke(null, banner),
							false);
					if (entityitem != null) {
						entityitem.getClass().getMethod("r").invoke(entityitem);
						entityitem.getClass().getMethod("d", String.class).invoke(entityitem, pl.getName());
					}
					return;
				}
				toAddPattern = ((BannerMeta) a.getCurrentItem().getItemMeta()).getPattern(0).getPattern();
				color = true;
			}
			refreshItems();
		}

		private void refreshItems() {
			Map<Integer, ItemStack> cont = new HashMap<>();
			cont.clear();
			if (color) {
				int x = 1;
				int y = 1;
				for (DyeColor i : DyeColor.values()) {
					ItemStack is = new ItemStack(Material.INK_SACK, 1, (short) 0, i.getDyeData());
					ItemMeta im = is.getItemMeta();
					im.setDisplayName(i.toString());
					is.setItemMeta(im);
					cont.put(y * 9 + x, is);
					++x;
					if (x == 8) {
						x = 1;
						++y;
					}
				}
			} else if (!color) {
				ItemStack bannerC = banner.clone();
				ItemMeta bmm = bannerC.getItemMeta();
				bmm.setDisplayName("\u00a7aFinish");
				bannerC.setItemMeta(bmm);
				cont.put(4, bannerC);
				int x2 = 0;
				for (PatternType i : PatternType.values()) {
					ItemStack ly = new ItemStack(Material.BANNER);
					BannerMeta bm = (BannerMeta) ly.getItemMeta();
					bm.setBaseColor(DyeColor.WHITE);
					bm.addPattern(new Pattern(DyeColor.BLACK, i));
					bm.setDisplayName("\u00a7r" + i.toString());
					ly.setItemMeta(bm);
					cont.put(9 + x2, ly);
					++x2;
				}
			}
			show(cont);
		}
	}
}
