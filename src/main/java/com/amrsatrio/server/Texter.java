package com.amrsatrio.server;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class Texter {
	@SuppressWarnings("deprecation")
	public static boolean text(String text, Player pl, boolean vertical) {
		Location pll = pl.getLocation();
		char[] chars = text.toString().trim().toCharArray();
		ArrayList<String> toSet = new ArrayList<String>(8);
		try {
			for (int i = 0; i < 8; i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < chars.length; j++) {
					sb.append(getTextFont(chars[j])[i]);
					sb.append(" ");
				}
				toSet.add(sb.toString());
			}
		} catch (IllegalArgumentException e) {
			pl.sendMessage("Server> \u00a7c" + e.getMessage());
			return true;
		}
		int rpl = 0;
		int rplna = 0;
		int var24 = Utils.floor(pll.getYaw() * 4.0F / 360.0F + 0.5D) & 3;// swne
		ItemStack setMat = pl.getInventory().getItemInMainHand();
		if (setMat == null || setMat.getTypeId() > 0xff) {
			pl.sendMessage("Server> Hold the block in your main hand that you want to set as the material.");
			return true;
		}
		for (int i = 0; i < 8; i++) {
			char[] ca = toSet.get(i).toCharArray();
			for (int j = 0; j < ca.length; j++) {
				Material set = ca[j] == 'x' ? setMat.getType() : Material.AIR;
				byte setd = ca[j] == 'x' ? setMat.getData().getData() : 0;
				Block curblock = null;
				if (pll.getPitch() > 45) {
					switch (var24) {
						case 0:
							// -x
							curblock = pl.getWorld().getBlockAt(pll.getBlockX() - 8 + i, pll.getBlockY() - 1 - j, pll.getBlockZ());
							break;
						case 1:
							// -z
							curblock = pl.getWorld().getBlockAt(pll.getBlockX(), pll.getBlockY() - 1 - j, pll.getBlockZ() - 8 + i);
							break;
						case 2:
							// +x
							curblock = pl.getWorld().getBlockAt(pll.getBlockX() + 8 - i, pll.getBlockY() - 1 - j, pll.getBlockZ());
							break;
						case 3:
							// +z
							curblock = pl.getWorld().getBlockAt(pll.getBlockX(), pll.getBlockY() - 1 - j, pll.getBlockZ() + 8 - i);
							break;
					}
				} else if (pll.getPitch() < -45) {
					switch (var24) {
						case 0:
							// +x
							curblock = pl.getWorld().getBlockAt(pll.getBlockX() + 8 - i, pll.getBlockY() + 1 + j, pll.getBlockZ());
							break;
						case 1:
							// +z
							curblock = pl.getWorld().getBlockAt(pll.getBlockX(), pll.getBlockY() + 1 + j, pll.getBlockZ() + 8 - i);
							break;
						case 2:
							// -x
							curblock = pl.getWorld().getBlockAt(pll.getBlockX() - 8 + i, pll.getBlockY() + 1 + j, pll.getBlockZ());
							break;
						case 3:
							// -z
							curblock = pl.getWorld().getBlockAt(pll.getBlockX(), pll.getBlockY() + 1 + j, pll.getBlockZ() - 8 + i);
							break;
					}
				} else {
					switch (var24) {
						case 0:
							// -x
							curblock = pl.getWorld().getBlockAt(pll.getBlockX() - 1 - j, pll.getBlockY() + 8 - i, pll.getBlockZ());
							break;
						case 1:
							// -z
							curblock = pl.getWorld().getBlockAt(pll.getBlockX(), pll.getBlockY() + 8 - i, pll.getBlockZ() - 1 - j);
							break;
						case 2:
							// +x
							curblock = pl.getWorld().getBlockAt(pll.getBlockX() + 1 + j, pll.getBlockY() + 8 - i, pll.getBlockZ());
							break;
						case 3:
							// +z
							curblock = pl.getWorld().getBlockAt(pll.getBlockX(), pll.getBlockY() + 8 - i, pll.getBlockZ() + 1 + j);
							break;
					}
				}
				if (curblock.getType() != set || curblock.getData() != setd) {
					rpl++;
				}
				if (curblock.getType() != Material.AIR) {
					rplna++;
				}
				curblock.setType(set);
				curblock.setData(setd);
			}
		}
		pl.sendMessage(String.format("Server> Created the text \"%s\" (%d character(s)) by changing %d blocks (%d non-air blocks).", new Object[]{text.toString().trim(), Integer.valueOf(chars.length), Integer.valueOf(rpl), Integer.valueOf(rplna)}));
		return true;
	}

	private static String[] getTextFont(char text) {
		switch (text) {
			case ' ':
				return new String[]{"   ", "   ", "   ", "   ", "   ", "   ", "   ", "   "};
			case '!':
				return new String[]{"x", "x", "x", "x", "x", " ", "x", " "};
			case '"':
				return new String[]{" x x", " x x", "x x ", "    ", "    ", "    ", "    ", "    "};
			case '#':
				return new String[]{" x x ", " x x ", "xxxxx", " x x ", "xxxxx", " x x ", " x x ", "     "};
			case '$':
				return new String[]{"  x  ", " xxxx", "x    ", " xxx ", "    x", "xxxx ", "  x  ", "     "};
			case '%':
				return new String[]{"x   x", "x  x ", "   x ", "  x  ", " x   ", " x  x", "x   x", "     "};
			case '&':
				return new String[]{"  x  ", " x x ", "  x  ", " xx x", "x xx ", "x  x ", " xx x", "     "};
			case '\'':
				return new String[]{" x", " x", "x ", "  ", "  ", "  ", "  ", "  "};
			case '(':
				return new String[]{"  xx", " x  ", "x   ", "x   ", "x   ", " x  ", "  xx", "    "};
			case ')':
				return new String[]{"xx  ", "  x ", "   x", "   x", "   x", "  x ", "xx  ", "    "};
			case '*':
				return new String[]{"    ", "    ", "x  x", " xx ", "x  x", "    ", "    ", "    "};
			case '+':
				return new String[]{"     ", "  x  ", "  x  ", "xxxxx", "  x  ", "  x  ", "     ", "     "};
			case ',':
				return new String[]{" ", " ", " ", " ", " ", "x", "x", "x"};
			case '-':
				return new String[]{"     ", "     ", "     ", "xxxxx", "     ", "     ", "     ", "     "};
			case '.':
				return new String[]{" ", " ", " ", " ", " ", "x", "x", " "};
			case '/':
				return new String[]{"    x", "   x ", "   x ", "  x  ", " x   ", " x   ", "x    ", "     "};
			case '0':
				return new String[]{" xxx ", "x   x", "x  xx", "x x x", "xx  x", "x   x", " xxx ", "     "};
			case '1':
				return new String[]{"  x  ", " xx  ", "  x  ", "  x  ", "  x  ", "  x  ", "xxxxx", "     "};
			case '2':
				return new String[]{" xxx ", "x   x", "    x", "  xx ", " x   ", "x   x", "xxxxx", "     "};
			case '3':
				return new String[]{" xxx ", "x   x", "    x", "  xx ", "    x", "x   x", " xxx ", "     "};
			case '4':
				return new String[]{"   xx", "  x x", " x  x", "x   x", "xxxxx", "    x", "    x", "     "};
			case '5':
				return new String[]{"xxxxx", "x    ", "xxxx ", "    x", "    x", "x   x", " xxx ", "     "};
			case '6':
				return new String[]{"  xx ", " x   ", "x    ", "xxxx ", "x   x", "x   x", " xxx ", "     "};
			case '7':
				return new String[]{"xxxxx", "x   x", "    x", "   x ", "  x  ", "  x  ", "  x  ", "     "};
			case '8':
				return new String[]{" xxx ", "x   x", "x   x", " xxx ", "x   x", "x   x", " xxx ", "     "};
			case '9':
				return new String[]{" xxx ", "x   x", "x   x", " xxxx", "    x", "   x ", " xx  ", "     "};
			case ':':
				return new String[]{" ", "x", "x", " ", " ", "x", "x", " "};
			case ';':
				return new String[]{" ", "x", "x", " ", " ", "x", "x", "x"};
			case '<':
				return new String[]{"   x", "  x ", " x  ", "x   ", " x  ", "  x ", "   x", "    "};
			case '=':
				return new String[]{"     ", "     ", "xxxxx", "     ", "     ", "xxxxx", "     ", "     "};
			case '>':
				return new String[]{"x   ", " x  ", "  x ", "   x", "  x ", " x  ", "x   ", "    "};
			case '?':
				return new String[]{" xxx ", "x   x", "    x", "   x ", "  x  ", "     ", "  x  ", "     "};
			case '@':
				return new String[]{" xxxx ", "x    x", "x xx x", "x xx x", "x xxxx", "x     ", " xxxx ", "      "};
			case 'A':
				return new String[]{" xxx ", "x   x", "xxxxx", "x   x", "x   x", "x   x", "x   x", "     "};
			case 'B':
				return new String[]{"xxxx ", "x   x", "xxxx ", "x   x", "x   x", "x   x", "xxxx ", "     "};
			case 'C':
				return new String[]{" xxx ", "x   x", "x    ", "x    ", "x    ", "x   x", " xxx ", "     "};
			case 'D':
				return new String[]{"xxxx ", "x   x", "x   x", "x   x", "x   x", "x   x", "xxxx ", "     "};
			case 'E':
				return new String[]{"xxxxx", "x    ", "xxx  ", "x    ", "x    ", "x    ", "xxxxx", "     "};
			case 'F':
				return new String[]{"xxxxx", "x    ", "xxx  ", "x    ", "x    ", "x    ", "x    ", "     "};
			case 'G':
				return new String[]{" xxxx", "x    ", "x  xx", "x   x", "x   x", "x   x", " xxx ", "     "};
			case 'H':
				return new String[]{"x   x", "x   x", "xxxxx", "x   x", "x   x", "x   x", "x   x", "     "};
			case 'I':
				return new String[]{"xxx", " x ", " x ", " x ", " x ", " x ", "xxx", "   "};
			case 'J':
				return new String[]{"    x", "    x", "    x", "    x", "    x", "x   x", " xxx ", "     "};
			case 'K':
				return new String[]{"x   x", "x  x ", "xxx  ", "x  x ", "x   x", "x   x", "x   x", "     "};
			case 'L':
				return new String[]{"x    ", "x    ", "x    ", "x    ", "x    ", "x    ", "xxxxx", "     "};
			case 'M':
				return new String[]{"x   x", "xx xx", "x x x", "x   x", "x   x", "x   x", "x   x", "     "};
			case 'N':
				return new String[]{"x   x", "xx  x", "x x x", "x  xx", "x   x", "x   x", "x   x", "     "};
			case 'O':
				return new String[]{" xxx ", "x   x", "x   x", "x   x", "x   x", "x   x", " xxx ", "     "};
			case 'P':
				return new String[]{"xxxx ", "x   x", "xxxx ", "x    ", "x    ", "x    ", "x    ", "     "};
			case 'Q':
				return new String[]{" xxx ", "x   x", "x   x", "x   x", "x   x", "x  x ", " xx x", "     "};
			case 'R':
				return new String[]{"xxxx ", "x   x", "xxxx ", "x   x", "x   x", "x   x", "x   x", "     "};
			case 'S':
				return new String[]{" xxxx", "x    ", " xxx ", "    x", "    x", "x   x", " xxx ", "     "};
			case 'T':
				return new String[]{"xxxxx", "  x  ", "  x  ", "  x  ", "  x  ", "  x  ", "  x  ", "     "};
			case 'U':
				return new String[]{"x   x", "x   x", "x   x", "x   x", "x   x", "x   x", " xxx ", "     "};
			case 'V':
				return new String[]{"x   x", "x   x", "x   x", "x   x", " x x ", " x x ", "  x  ", "     "};
			case 'W':
				return new String[]{"x   x", "x   x", "x   x", "x   x", "x x x", "xx xx", "x   x", "     "};
			case 'X':
				return new String[]{"x   x", " x x ", "  x  ", " x x ", "x   x", "x   x", "x   x", "     "};
			case 'Y':
				return new String[]{"x   x", " x x ", "  x  ", "  x  ", "  x  ", "  x  ", "  x  ", "     "};
			case 'Z':
				return new String[]{"xxxxx", "    x", "   x ", "  x  ", " x   ", "x    ", "xxxxx", "     "};
			case '[':
				return new String[]{"xxx", "x  ", "x  ", "x  ", "x  ", "x  ", "xxx", "   "};
			case '\\':
				return new String[]{"x    ", " x   ", " x   ", "  x  ", "   x ", "   x ", "    x", "     "};
			case ']':
				return new String[]{"xxx", "  x", "  x", "  x", "  x", "  x", "xxx", "   "};
			case '^':
				return new String[]{"  x  ", " x x ", "x   x", "     ", "     ", "     ", "     ", "     "};
			case '_':
				return new String[]{"     ", "     ", "     ", "     ", "     ", "     ", "     ", "xxxxx"};
			case '`':
				return new String[]{"x ", "x ", " x", "  ", "  ", "  ", "  ", "  "};
			case 'a':
				return new String[]{"     ", "     ", " xxx ", "    x", " xxxx", "x   x", " xxxx", "     "};
			case 'b':
				return new String[]{"x    ", "x    ", "x xx ", "xx  x", "x   x", "x   x", "xxxx ", "     "};
			case 'c':
				return new String[]{"     ", "     ", " xxx ", "x   x", "x    ", "x   x", " xxx ", "     "};
			case 'd':
				return new String[]{"    x", "    x", " xx x", "x  xx", "x   x", "x   x", " xxxx", "     "};
			case 'e':
				return new String[]{"     ", "     ", " xxx ", "x   x", "xxxxx", "x    ", " xxxx", "     "};
			case 'f':
				return new String[]{"  xx", " x  ", "xxxx", " x  ", " x  ", " x  ", " x  ", "    "};
			case 'g':
				return new String[]{"     ", "     ", " xxxx", "x   x", "x   x", " xxxx", "    x", "xxxx "};
			case 'h':
				return new String[]{"x    ", "x    ", "x xx ", "xx  x", "x   x", "x   x", "x   x", "     "};
			case 'i':
				return new String[]{"x", " ", "x", "x", "x", "x", "x", " "};
			case 'j':
				return new String[]{"    x", "     ", "    x", "    x", "    x", "x   x", "x   x", " xxx "};
			case 'k':
				return new String[]{"x   ", "x   ", "x  x", "x x ", "xx  ", "x x ", "x  x", "    "};
			case 'l':
				return new String[]{"x ", "x ", "x ", "x ", "x ", "x ", " x", "  "};
			case 'm':
				return new String[]{"     ", "     ", "xx x ", "x x x", "x x x", "x   x", "x   x", "     "};
			case 'n':
				return new String[]{"     ", "     ", "xxxx ", "x   x", "x   x", "x   x", "x   x", "     "};
			case 'o':
				return new String[]{"     ", "     ", " xxx ", "x   x", "x   x", "x   x", " xxx ", "     "};
			case 'p':
				return new String[]{"     ", "     ", "x xx ", "xx  x", "x   x", "xxxx ", "x    ", "x    "};
			case 'q':
				return new String[]{"     ", "     ", " xx x", "x  xx", "x   x", " xxxx", "    x", "    x"};
			case 'r':
				return new String[]{"     ", "     ", "x xx ", "xx  x", "x    ", "x    ", "x    ", "     "};
			case 's':
				return new String[]{"     ", "     ", " xxxx", "x    ", " xxx ", "    x", "xxxx ", "     "};
			case 't':
				return new String[]{" x ", " x ", "xxx", " x ", " x ", " x ", "  x", "   "};
			case 'u':
				return new String[]{"     ", "     ", "x   x", "x   x", "x   x", "x   x", " xxxx", "     "};
			case 'v':
				return new String[]{"     ", "     ", "x   x", "x   x", "x   x", " x x ", "  x  ", "     "};
			case 'w':
				return new String[]{"     ", "     ", "x   x", "x   x", "x x x", "x x x", " xxxx", "     "};
			case 'x':
				return new String[]{"     ", "     ", "x   x", " x x ", "  x  ", " x x ", "x   x", "     "};
			case 'y':
				return new String[]{"     ", "     ", "x   x", "x   x", "x   x", " xxxx", "    x", "xxxx "};
			case 'z':
				return new String[]{"     ", "     ", "xxxxx", "   x ", "  x  ", " x   ", "xxxxx", "     "};
			case '{':
				return new String[]{"  xx", " x  ", " x  ", "x   ", " x  ", " x  ", "  xx", "    "};
			case '|':
				return new String[]{"x", "x", "x", " ", "x", "x", "x", " "};
			case '}':
				return new String[]{"xx  ", "  x ", "  x ", "   x", "  x ", "  x ", "xx  ", "    "};
			case '~':
				return new String[]{" xx  x", "x  xx ", "      ", "      ", "      ", "      ", "      ", "      "};
		}
		throw new IllegalArgumentException("The character " + text + " is not supported");
	}
}