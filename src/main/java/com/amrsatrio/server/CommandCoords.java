package com.amrsatrio.server;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;

//TODO: REFLECTION
public class CommandCoords implements CommandExecutor {
	private static final Joiner a = Joiner.on(',');
	private static Function<Map.Entry<IBlockState<?>, Comparable<?>>, String> b;

	static {
		try {
			Field field = BlockDataAbstract.class.getDeclaredField("b");
			field.setAccessible(true);
			b = (Function<Map.Entry<IBlockState<?>, Comparable<?>>, String>) field.get(null);
		} catch (Throwable e) {
			AmrsatrioServer.LOGGER.warn("Can't get BlockState function", e);
		}
	}

	@Override
	public boolean onCommand(CommandSender commandsender, Command command, String s, String[] astring) {
//		Utils.noReflection(commandsender);
		boolean flag = commandsender instanceof Player;

		if (!flag) {
			return true;
		}

		Player player = (Player) commandsender;
		RayTrace raytrace = RayTrace.a(player, 5.0D);

		if (raytrace.a()) {
			IBlockData iblockdata = ((CraftWorld) player.getWorld()).getHandle().getType(raytrace.f());
			String s1 = Block.REGISTRY.b(iblockdata.getBlock()).toString();
			BlockPosition blockposition = raytrace.f();
			player.sendMessage(String.format("\u00a7b--- Details of block at %d, %d, %d ---", blockposition.getX(), blockposition.getY(), blockposition.getZ()));
			commandsender.sendMessage(" \u00a77Name: \u00a7r" + s1);
			Map<IBlockState<?>, Comparable<?>> map = iblockdata.u();

			if (!map.isEmpty()) {
				commandsender.sendMessage(" \u00a77Block state: \u00a7r");
			}

			for (Map.Entry<IBlockState<?>, Comparable<?>> map$entry : map.entrySet()) {
				String s2 = map$entry.getValue().toString();
				if (map$entry.getValue() == Boolean.TRUE) {
					s2 = "\u00a7a" + s2;
				} else if (map$entry.getValue() == Boolean.FALSE) {
					s2 = "\u00a7c" + s2;
				}
				commandsender.sendMessage(" - \u00a77" + map$entry.getKey().a() + ": \u00a7r" + s2);
			}

			if (iblockdata.getBlock().isTileEntity()) {
				commandsender.sendMessage(" \u00a77Block data: \u00a7r" + ((CraftWorld) player.getWorld()).getHandle().getTileEntity(blockposition).save(new NBTTagCompound()));
			}

			if (s1.startsWith("minecraft:")) {
				s1 = s1.substring("minecraft:".length());
			}

			StringBuilder stringbuilder = new StringBuilder(String.format("/setblock %d %d %d %s", blockposition.getX(), blockposition.getY(), blockposition.getZ(), s1));

			if (!map.isEmpty()) {
				stringbuilder.append(" ");
				a.appendTo(stringbuilder, Iterables.transform(map.entrySet(), b));
			}

			String setblockButton = Utils.actionBoxJson("setblock", stringbuilder.toString());
			Utils.jsonMsg(player, String.format("[{\"text\":\" \u00a77Actions: \"},%s]", setblockButton), false);
		} else {
			AmrsatrioServer.msg(player, "You're not pointing at a block.");
		}

		return true;
	}
}
