package com.amrsatrio.server.command;

import net.minecraft.server.v1_11_R1.CommandException;

public class NonPlayerException extends CommandException {
	public NonPlayerException() {
		super("You must be a player in order to execute this command");
	}
}
