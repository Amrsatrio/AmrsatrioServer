package com.amrsatrio.server;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;

@SuppressWarnings("serial")
public class WrongUsageException extends CommandException {
	public WrongUsageException(String cause, Command command, String label) {
		super((cause == null ? "" : cause + ". ") + "Usage: " + command.getUsage().replaceFirst("<command>", label));
	}

	public WrongUsageException(Command command, String label) {
		this(null, command, label);
	}
}
