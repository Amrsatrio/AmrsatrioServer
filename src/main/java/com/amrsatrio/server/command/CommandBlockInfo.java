package com.amrsatrio.server.command;

import com.amrsatrio.server.RayTrace;
import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.Utils;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//TODO: REFLECTION
public class CommandBlockInfo extends CustomizedPluginCommand {
	private static final Joiner a = Joiner.on(',');
	private static Function<Entry<IBlockState<?>, Comparable<?>>, String> b;

	static {
		try {
			Field field = BlockDataAbstract.class.getDeclaredField("b");
			field.setAccessible(true);
			b = (Function<Entry<IBlockState<?>, Comparable<?>>, String>) field.get(null);
		} catch (Throwable e) {
			ServerPlugin.LOGGER.warn("Can't get BlockState function", e);
		}
	}

	public CommandBlockInfo() {
		super("blockinfo", "Shows the block-at-the-specified-coordinates' info.", "/<command> [<x> <y> <z>]", new ArrayList<String>());
	}

	@Override
	public boolean executePluginCommand(CommandSender commandsender, ICommandListener icommandlistener, String s, String[] astring) throws CommandException {
		BlockPosition blockposition = null;

		if (astring.length < 3) {
			if (commandsender instanceof Player) {
				RayTrace raytrace = RayTrace.a((Player) commandsender, 5.0D);

				if (raytrace.a() && raytrace.f() != null) {
					blockposition = raytrace.f();
				}
			} else {
				throw new ExceptionUsage(getUsage(s));
			}
		} else {
			blockposition = CommandAbstract.a(icommandlistener, astring, 0, false);
		}

		if (blockposition == null) {
			throw new CommandException("You're not pointing at a block");
		}

		if (!icommandlistener.getWorld().isLoaded(blockposition)) {
			commandsender.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + "Warning: The location you're accessing is not loaded yet");
		}

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

			commandsender.sendMessage(ChatColor.GRAY + " · " + map$entry.getKey().a() + ": " + ChatColor.RESET + s2);
		}

		if (iblockdata.getBlock().isTileEntity()) {
			commandsender.sendMessage(ChatColor.GRAY + "Block entity data: " + ChatColor.RESET + icommandlistener.getWorld().getTileEntity(blockposition).save(new NBTTagCompound()));
		}
		if (s1.startsWith("minecraft:")) {
			s1 = s1.substring("minecraft:".length());
		}

		StringBuilder stringbuilder = new StringBuilder(String.format("/setblock %d %d %d %s", blockposition.getX(), blockposition.getY(), blockposition.getZ(), s1));

		if (!map.isEmpty()) {
			stringbuilder.append(" ");
			a.appendTo(stringbuilder, Iterables.transform(map.entrySet(), b));
		}

		String s2 = stringbuilder.toString();

		if (commandsender instanceof Player) {
			icommandlistener.sendMessage(Utils.plsRenameMe("Actions", Utils.suggestBoxJson("setblock", s2, false)));
		} else {
			commandsender.sendMessage(ChatColor.GRAY + "Set block command: " + ChatColor.RESET + s2);
		}

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) throws IllegalArgumentException {
		return args.length > 0 && args.length <= 3 ? CommandAbstract.a(args, 0, location == null ? null : new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ())) : Collections.<String>emptyList();
	}
}
