package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.Utils;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CommandTpWorld extends CustomizedPluginCommand {
	private static final String TAG = "Teleporter";

	public CommandTpWorld() {
		super("tpworld", "Teleports you to another world. Highly experimental.", "/<command> [worldName...|index]", Arrays.asList("changeworld", "tpw"));
	}

	@Override
	public boolean executePluginCommand(final CommandSender commandsender, ICommandListener icommandlistener, String s, String[] astring) throws CommandException {
		if (!(commandsender instanceof Player)) {
			throw new NonPlayerException();
		}

		List<String> list = Utils.getExistingWorlds();

		if (astring.length == 0) {
			icommandlistener.sendMessage(createMessage(new ChatComponentText("Existing worlds are:"), TAG));
			Utils.printListNumbered(commandsender, list);
			return true;
		}

		Player player = (Player) commandsender;
		String s1 = a(list, astring);
		IChatBaseComponent ichatbasecomponent = new ChatComponentText(s1).setChatModifier(new ChatModifier().setBold(true));
//		String worldbefore = player.getWorld().getName();

		if (!list.contains(s1)) {
			icommandlistener.sendMessage(createMessage(new ChatMessage(Messages.Commands.TPW_NONEXISTENT_TITLE, ichatbasecomponent), TAG));
			Utils.printListNumbered(commandsender, list);
			return true;
		}

		icommandlistener.sendMessage(createMessage(new ChatMessage(Messages.Commands.TPW_TELEPORTING, ichatbasecomponent), TAG));
		World world = Bukkit.getWorld(s1);

		if (world == null) {
			Bukkit.getWorlds().add(world = Bukkit.createWorld(new WorldCreator(s1)));
		}

		// TODO make the player tp'd to its last position in that world instead of to the spawnpoint
		player.teleport(new Location(world, world.getSpawnLocation().getX(), world.getSpawnLocation().getY(), world.getSpawnLocation().getZ()));
//		msg(commandsender, "WARNING: This feature is very experimental. The scoreboard in " + worldbefore + " are mixed with the scoreboard in " + s1 + ". So don't try to teleport to lots-of-command-blocks maps!");
		IChatBaseComponent ichatbasecomponent1 = new ChatMessage(Messages.Commands.TPW_SUCCESS, commandsender.getName(), ichatbasecomponent);
		icommandlistener.sendMessage(createMessage(ichatbasecomponent1, TAG));
		broadcastCommandMessage(commandsender, ichatbasecomponent1.getText(), false);
		return true;
	}

	private String a(List<String> list, String[] astring) {
		try {
			return list.get(Integer.parseInt(astring[0]) - 1);
		} catch (NumberFormatException e) {
			return Utils.buildString(astring, 0);
		}
	}

//	private static final Function<ChatColor, EnumChatFormat> TO_NMS = new Function<ChatColor, EnumChatFormat>() {
//		@Nullable
//		@Override
//		public EnumChatFormat apply(@Nullable ChatColor chatColor) {
//			return EnumChatFormat.b(chatColor == null ? null : chatColor.toString());
//		}
//	};

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		return CommandAbstract.a(args, Utils.getExistingWorlds());
	}
}
