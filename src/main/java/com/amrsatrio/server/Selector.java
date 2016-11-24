package com.amrsatrio.server;

import net.minecraft.server.v1_11_R1.Entity;
import org.bukkit.command.CommandSender;

import java.util.List;

public class Selector {
	public static List<Entity> select(CommandSender a, String string) {
		throw new UnsupportedOperationException("I'm updating this command to 1.11!");
//		ICommandListener icommandlistener = Utils.getListener(a);
//		Matcher matcher = Pattern.compile("^@([pare])(?:\\[([\\w=,!-]*)\\])?$").matcher(string);
//		a.sendMessage("DEBUG matches = " + (matcher.matches() && icommandlistener.a(1, "@")));
//		return PlayerSelector.getPlayers(icommandlistener, string, Entity.class);
	}
}
