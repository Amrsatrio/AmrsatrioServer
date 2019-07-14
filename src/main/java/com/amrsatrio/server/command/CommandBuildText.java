package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandBuildText extends AbstractBrigadierCommand {
	private static final int TEXT_HEIGHT = 8;

	public CommandBuildText() {
		super("buildtext", "Make blocks forming a text.", Collections.singletonList("text"));
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().requires(requireCheatsEnabled()).then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("text", StringArgumentType.greedyString()).executes(context -> text(context.getSource(), StringArgumentType.getString(context, "text"), false)));
	}

	public static int text(CommandListenerWrapper listener, String text, boolean forceReplace) throws CommandSyntaxException {
		Player pl = listener.h().getBukkitEntity();
		Location playerLocation = pl.getLocation();
		char[] chars = text.trim().toCharArray();
		ArrayList<String> toSet = new ArrayList<>(8);

		for (int i = 0; i < TEXT_HEIGHT; i++) {
			StringBuilder sb = new StringBuilder();
			for (char aChar : chars) {
				sb.append(getTextFont(aChar)[i]);
				sb.append(" ");
			}
			toSet.add(sb.toString());
		}

		int replaced = 0, replacedNonAir = 0;
		int direction = Utils.floor(playerLocation.getYaw() * 4.0F / 360.0F + 0.5D) & 3;// swne
		ItemStack setMat = pl.getInventory().getItemInMainHand();

		if (setMat == null || !setMat.getType().isBlock()) {
			throw Messages.NO_BLOCK_TO_SET_ERROR.create();
		}

		Map<Block, Material> modifyMap = new HashMap<>();
		List<Location> nonAirBlocksNeedToBeRemoved = new ArrayList<>();

		for (int i = 0; i < TEXT_HEIGHT; i++) {
			char[] charArray = toSet.get(i).toCharArray();

			for (int j = 0; j < charArray.length; j++) {
				Location setLocation = new Location(playerLocation.getWorld(), 0, 0, 0);

				if (playerLocation.getPitch() > 45) {
					switch (direction) {
						case 0:
							// -x
							setXYZ(setLocation, playerLocation.getBlockX() - 8 + i, playerLocation.getBlockY() - 1 - j, playerLocation.getBlockZ());
							break;
						case 1:
							// -z
							setXYZ(setLocation, playerLocation.getBlockX(), playerLocation.getBlockY() - 1 - j, playerLocation.getBlockZ() - 8 + i);
							break;
						case 2:
							// +x
							setXYZ(setLocation, playerLocation.getBlockX() + 8 - i, playerLocation.getBlockY() - 1 - j, playerLocation.getBlockZ());
							break;
						case 3:
							// +z
							setXYZ(setLocation, playerLocation.getBlockX(), playerLocation.getBlockY() - 1 - j, playerLocation.getBlockZ() + 8 - i);
							break;
					}
				} else if (playerLocation.getPitch() < -45) {
					switch (direction) {
						case 0:
							// +x
							setXYZ(setLocation, playerLocation.getBlockX() + 8 - i, playerLocation.getBlockY() + 1 + j, playerLocation.getBlockZ());
							break;
						case 1:
							// +z
							setXYZ(setLocation, playerLocation.getBlockX(), playerLocation.getBlockY() + 1 + j, playerLocation.getBlockZ() + 8 - i);
							break;
						case 2:
							// -x
							setXYZ(setLocation, playerLocation.getBlockX() - 8 + i, playerLocation.getBlockY() + 1 + j, playerLocation.getBlockZ());
							break;
						case 3:
							// -z
							setXYZ(setLocation, playerLocation.getBlockX(), playerLocation.getBlockY() + 1 + j, playerLocation.getBlockZ() - 8 + i);
							break;
					}
				} else {
					switch (direction) {
						case 0:
							// -x
							setXYZ(setLocation, playerLocation.getBlockX() - 1 - j, playerLocation.getBlockY() + 8 - i, playerLocation.getBlockZ());
							break;
						case 1:
							// -z
							setXYZ(setLocation, playerLocation.getBlockX(), playerLocation.getBlockY() + 8 - i, playerLocation.getBlockZ() - 1 - j);
							break;
						case 2:
							// +x
							setXYZ(setLocation, playerLocation.getBlockX() + 1 + j, playerLocation.getBlockY() + 8 - i, playerLocation.getBlockZ());
							break;
						case 3:
							// +z
							setXYZ(setLocation, playerLocation.getBlockX(), playerLocation.getBlockY() + 8 - i, playerLocation.getBlockZ() + 1 + j);
							break;
					}
				}

				Block blockAt = playerLocation.getWorld().getBlockAt(setLocation);

				if (!forceReplace && blockAt.getType() != Material.AIR) {
					nonAirBlocksNeedToBeRemoved.add(setLocation);
					continue;
				}

				modifyMap.put(blockAt, charArray[j] == 'x' ? setMat.getType() : Material.AIR);
			}
		}

		if (!nonAirBlocksNeedToBeRemoved.isEmpty()) {
			throw Messages.BLOCKED_BY_BLOCK_ERROR.create(nonAirBlocksNeedToBeRemoved);
//			return Command.SINGLE_SUCCESS;
		}

		for (Map.Entry<Block, Material> entry : modifyMap.entrySet()) {
			Block curblock = entry.getKey();

			if (curblock.getType() != entry.getValue()) {
				replaced++;
			}

			if (curblock.getType() != Material.AIR) {
				replacedNonAir++;
			}

			curblock.setType(entry.getValue());
		}

		ChatMessage successMsg;

		if (forceReplace) {
			successMsg = new ChatMessage("Created the text \"%s\" (%s character(s)) by replacing %s blocks (%s non-air).", text.trim(), chars.length, replaced, replacedNonAir);
		} else {
			successMsg = new ChatMessage("Created the text \"%s\" (%s character(s)) by replacing %s blocks.", text.trim(), chars.length, replaced);
		}

		listener.sendMessage(successMsg, true);
		return Command.SINGLE_SUCCESS;
	}

	private static void setXYZ(Location location, int x, int y, int z) {
		location.setX(x);
		location.setY(y);
		location.setZ(z);
	}

	private static String[] getTextFont(char character) throws CommandSyntaxException {
		switch (character) {
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

		throw Messages.UNSUPPORTED_CHAR_ERROR.create(character);
	}
}