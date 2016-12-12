package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.Utils;
import net.minecraft.server.v1_11_R1.*;
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
		super("tpworld", "Teleports you to another world. Highly experimental.", "/<command> <worldName...>", Arrays.asList("changeworld", "tpw"));
	}

	@Override
	public boolean executePluginCommand(final CommandSender commandsender, ICommandListener icommandlistener, String s, String[] astring) throws CommandException {
		if (!(commandsender instanceof Player)) {
			throw new NonPlayerException();
		}

		if (astring.length == 0) {
			throw new ExceptionUsage(getUsage(s));
		}

		Player p = (Player) commandsender;
		String worldname = Utils.buildString(astring, 0);
		IChatBaseComponent worldNameAsComponent = new ChatComponentText(worldname).setChatModifier(new ChatModifier().setBold(true));
//		String worldbefore = p.getWorld().getName();
		List<String> exworlds = Utils.getExistingWorlds();

		if (!exworlds.contains(worldname)) {
			icommandlistener.sendMessage(createMessage(new ChatMessage(Messages.Commands.TPW_NONEXISTENT_TITLE, worldNameAsComponent), TAG));
			Utils.printListNumbered(commandsender, exworlds);
			return true;
		}

		icommandlistener.sendMessage(createMessage(new ChatMessage(Messages.Commands.TPW_TELEPORTING, worldNameAsComponent), TAG));
		World w = Bukkit.getWorld(worldname);

		if (w == null) {
			Bukkit.getWorlds().add(w = Bukkit.createWorld(new WorldCreator(worldname)));
		}

		p.teleport(new Location(w, w.getSpawnLocation().getX(), w.getSpawnLocation().getY(), w.getSpawnLocation().getZ()));
//		msg(commandsender, "WARNING: This feature is very experimental. The scoreboard in " + worldbefore + " are mixed with the scoreboard in " + worldname + ". So don't try to teleport to lots-of-command-blocks maps!");
		IChatBaseComponent s1 = new ChatMessage(Messages.Commands.TPW_SUCCESS, commandsender.getName(), worldNameAsComponent);
		icommandlistener.sendMessage(createMessage(s1, TAG));
		broadcastCommandMessage(commandsender, s1.getText(), false);
		return true;
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
