package com.amrsatrio.server.command;

import com.amrsatrio.server.ServerPlugin;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommandGetBanner implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		boolean isPlayer = sender instanceof Player;

		if (!isPlayer) {
			return true;
		}

		Player p = (Player) sender;
		GetBannerGui gbg = new GetBannerGui(p);
		gbg.refreshItems();
		ServerPlugin.getBannerInstances.put(p, gbg);
		return true;
	}

	public static class GetBannerGui {
		public boolean switching = false;
		private Player pl;
		private boolean color = true;
		private boolean first = true;
		private ItemStack banner = new ItemStack(Material.BANNER);
		private PatternType toAddPattern;

		public GetBannerGui(Player a) {
			pl = a;
		}

		public void show(Map<Integer, ItemStack> cont) {
			Inventory inv = Bukkit.createInventory(pl, 6 * 9, color ? first ? "Select Base Color" : "Select Layer Color" : "Add Layer");

			for (Entry<Integer, ItemStack> i : cont.entrySet()) {
				inv.setItem(i.getKey(), i.getValue());
			}

			switching = true;
			// pl.closeInventory();
			pl.openInventory(inv);
			switching = false;
		}

		public void handle(InventoryClickEvent a) throws Exception {
			if (a.getCurrentItem() == null || a.getCurrentItem().getItemMeta() == null) {
				return;
			}

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
			} else {
				if (a.getSlot() == 4) {
					pl.closeInventory();
					EntityPlayer entityplayer = ((CraftPlayer) pl).getHandle();
					EntityItem entityitem = entityplayer.drop(CraftItemStack.asNMSCopy(banner), false);

					if (entityitem != null) {
						entityitem.r();
						entityitem.d(pl.getName());
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
					bm.setDisplayName("\u00a7r" + i);
					ly.setItemMeta(bm);
					cont.put(9 + x2, ly);
					++x2;
				}
			}

			show(cont);
		}
	}
}
