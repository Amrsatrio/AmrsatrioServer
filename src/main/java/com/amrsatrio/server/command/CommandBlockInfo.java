package com.amrsatrio.server.command;

import com.amrsatrio.server.AmrsatrioServer;
import com.amrsatrio.server.RayTrace;
import com.amrsatrio.server.Utils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//TODO: REFLECTION
public class CommandBlockInfo extends Command {
	private static final Joiner a = Joiner.on(',');
	private static Function<Entry<IBlockState<?>, Comparable<?>>, String> b;

	static {
		try {
			Field field = BlockDataAbstract.class.getDeclaredField("b");
			field.setAccessible(true);
			b = (Function<Entry<IBlockState<?>, Comparable<?>>, String>) field.get(null);
		} catch (Throwable e) {
			AmrsatrioServer.LOGGER.warn("Can't get BlockState function", e);
		}
	}

	public CommandBlockInfo() {
		super("blockinfo", "Shows the block-at-the-specified-coordinates' info.", "/<command> [<x> <y> <z>]", new ArrayList<String>());
	}

	@Override
	public boolean execute(CommandSender commandsender, String s, String[] astring) {
//		Utils.noReflection(commandsender);
		ICommandListener icommandlistener = Utils.getListener(commandsender);

		try {
			BlockPosition blockposition = null;

			if (astring.length < 3) {
				if (commandsender instanceof Player) {
					RayTrace raytrace = RayTrace.a((Player) commandsender, 5.0D);

					if (raytrace.a() && raytrace.f() != null) {
						blockposition = raytrace.f();
					}
				} else {
					throw new ExceptionUsage(getUsage().replace("<command>", s));
				}
			} else {
				blockposition = CommandAbstract.a(icommandlistener, astring, 0, false);
			}

			if (blockposition == null) {
				throw new CommandException("You're not pointing at a block");
			}

//			if (!icommandlistener.getWorld().isLoaded(blockposition)) {
//				throw new CommandException("commands.clone.outOfWorld");
//			}

			IBlockData iblockdata = icommandlistener.getWorld().getType(blockposition);
			String s1 = Block.REGISTRY.b(iblockdata.getBlock()).toString();
			commandsender.sendMessage(String.format(ChatColor.AQUA + "--- Details of block at (%d, %d, %d) ---", blockposition.getX(), blockposition.getY(), blockposition.getZ()));
			commandsender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.RESET + s1);
			commandsender.sendMessage(ChatColor.GRAY + "Data value: " + ChatColor.RESET + iblockdata.getBlock().toLegacyData(iblockdata));
			Map<IBlockState<?>, Comparable<?>> map = iblockdata.u();

			if (!map.isEmpty()) {
				commandsender.sendMessage(ChatColor.GRAY + "Block state: " + ChatColor.RESET);
			}

			for (Entry<IBlockState<?>, Comparable<?>> map$entry : map.entrySet()) {
				String s2 = map$entry.getValue().toString();

				if (map$entry.getValue() == Boolean.TRUE) {
					s2 = ChatColor.GREEN + s2;
				} else if (map$entry.getValue() == Boolean.FALSE) {
					s2 = ChatColor.RED + s2;
				}

				commandsender.sendMessage(ChatColor.GRAY + " - " + map$entry.getKey().a() + ": " + ChatColor.RESET + s2);
			}

			if (iblockdata.getBlock().isTileEntity()) {
				commandsender.sendMessage(ChatColor.GRAY + "Block entity data: " + ChatColor.RESET + icommandlistener.getWorld().getTileEntity(blockposition).save(new NBTTagCompound()));
			}

			if (commandsender instanceof Player) {
				if (s1.startsWith("minecraft:")) {
					s1 = s1.substring("minecraft:".length());
				}

				StringBuilder stringbuilder = new StringBuilder(String.format("/setblock %d %d %d %s", blockposition.getX(), blockposition.getY(), blockposition.getZ(), s1));

				if (!map.isEmpty()) {
					stringbuilder.append(" ");
					a.appendTo(stringbuilder, Iterables.transform(map.entrySet(), b));
				}

				String setblockButton = Utils.actionBoxJson("setblock", stringbuilder.toString());
				Utils.jsonMsg((Player) commandsender, String.format("[{\"text\":\"\u00a77Actions: \"},%s]", setblockButton), false);
			}

			return true;
		} catch (CommandException commandexception) {
			IChatBaseComponent ichatbasecomponent = new ChatMessage(commandexception.getMessage(), commandexception.getArgs());
			ichatbasecomponent.getChatModifier().setColor(EnumChatFormat.RED);
			icommandlistener.sendMessage(ichatbasecomponent);
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
		return args.length > 0 && args.length <= 3 ? CommandAbstract.a(args, 0, location == null ? null : new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ())) : Collections.<String>emptyList();
	}
}
