package com.amrsatrio.server.command;

import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandException;

public class NonPlayerException extends CommandException {
	public NonPlayerException() {
		super(new ChatMessage("permissions.requires.player"));
	}
}
