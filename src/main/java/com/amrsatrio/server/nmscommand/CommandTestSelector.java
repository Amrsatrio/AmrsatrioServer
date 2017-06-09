package com.amrsatrio.server.nmscommand;

import net.minecraft.server.v1_12_R1.*;

import java.util.Arrays;
import java.util.List;

public class CommandTestSelector extends CommandAbstract {
	@Override
	public String getCommand() {
		return "testselector";
	}

	@Override
	public String getUsage(ICommandListener iCommandListener) {
		return "/" + getCommand() + " <expression>";
	}

	@Override
	public int a() {
		return 2;
	}

	@Override
	public void execute(MinecraftServer minecraftServer, ICommandListener iCommandListener, String[] strings) throws net.minecraft.server.v1_12_R1.CommandException {
		iCommandListener.sendMessage(new ChatMessage("DEBUG " + Arrays.toString(strings)));
		iCommandListener.sendMessage(new ChatMessage("DEBUG " + iCommandListener));

		if (strings.length < 1) {
			throw new ExceptionUsage(getUsage(iCommandListener));
		}

		List<Entity> list = PlayerSelector.getPlayers(iCommandListener, strings[0], Entity.class);
		iCommandListener.sendMessage(new ChatMessage("Selector expression " + strings[0] + " matches " + list.size() + " entities"));
	}

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}
}
