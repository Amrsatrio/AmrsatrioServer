package com.amrsatrio.server.command;

import net.minecraft.server.v1_12_R1.*;
import net.minecraft.server.v1_12_R1.MapIcon.Type;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static net.minecraft.server.v1_12_R1.MapIcon.Type.MANSION;
import static net.minecraft.server.v1_12_R1.MapIcon.Type.MONUMENT;

public class CommandGetExplorerMap extends CustomizedPluginCommand {
	private static final Random RANDOM = new Random();

	public CommandGetExplorerMap() {
		super("getexplorermap", "Gives you an random explorer map like the one given by cartographer villagers.", "/<command>", new ArrayList<>());
	}

	@Override
	public boolean executePluginCommand(CommandSender commandsender, ICommandListener icommandlistener, String s, String[] astring) throws CommandException {
		if (!(icommandlistener instanceof EntityPlayer)) {
			throw new NonPlayerException();
		}

		int i = RANDOM.nextInt(2);
		String s1 = new String[]{"Mansion", "Monument"}[i];
		Type mapicon$type = new Type[]{MANSION, MONUMENT}[i];
		World world = icommandlistener.getWorld();
		BlockPosition blockposition = world.a(s1, icommandlistener.getChunkCoordinates(), true);

		if (blockposition != null) {
			ItemStack itemstack = ItemWorldMap.a(world, blockposition.getX(), blockposition.getZ(), (byte) 2, true, true);
			ItemWorldMap.a(world, itemstack);
			WorldMap.a(itemstack, blockposition, "+", mapicon$type);
			itemstack.f("filled_map." + s1.toLowerCase(Locale.ROOT));
			Bukkit.dispatchCommand(commandsender, "give " + commandsender.getName() + " minecraft:filled_map 1 " + itemstack.getData() + ' ' + itemstack.getTag());
		}

		return true;
	}
}
