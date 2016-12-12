package com.amrsatrio.server.command;

import net.minecraft.server.v1_11_R1.CommandException;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class CommandThrowException extends CustomizedPluginCommand {
	public CommandThrowException() {
		super("throwexception", "Debugging command", "/<command>", new ArrayList<String>());
	}

	@Override
	public boolean executePluginCommand(CommandSender commandsender, String s, String[] astring) throws CommandException {
		throw new RuntimeException("You told me to do this, huh?");
	}
}
