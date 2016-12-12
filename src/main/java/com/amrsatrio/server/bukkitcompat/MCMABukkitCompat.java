package com.amrsatrio.server.bukkitcompat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

public class MCMABukkitCompat implements Listener {
	private Logger a;
	private JavaPlugin b;
	private PrintStream c;

	public MCMABukkitCompat(JavaPlugin javaplugin) {
		a = javaplugin.getLogger();
		b = javaplugin;
		c = new PrintStream(new FileOutputStream(FileDescriptor.err));
	}

	public void init() {
//		this.b.getCommand("tell").setExecutor(new Executor(c));
//		this.b.getCommand("kickreason").setExecutor(new Executor(c));
		b.getCommand("svping").setExecutor(new Executor(c));
		b.getCommand("pushcommand").setExecutor(new Executor(c));
		Bukkit.getPluginManager().registerEvents(this, b);
		c.println("0 0 [MCMAX] MCMACOMPAT R22A");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent playercommandpreprocessevent) {
		String s = playercommandpreprocessevent.getMessage().substring(1);
		String[] astring = s.split(" ", 2);
		String s1 = astring[0];
		/*
		 * String[] astring1 = { "accept", "addmember", "backupworld",
		 * "decline", "delmember", "delplayerdata", "giveme", "giveto", "goto",
		 * "invite", "join", "lastseen", "lookup", "motd", "opme", "players",
		 * "restart", "serverstats", "userstats", "whois", "creative",
		 * "survival", "serverversion", "day", "night" };
		 */
		String[] astring1 = {"accept", "addmember", "backupworld", "decline", "delmember", "delplayerdata", "giveme", "giveto", "goto", "invite", "join", "lastseen", "lookup", "motd", "opme", "players", "serverstats", "userstats", "whois", "creative", "survival", "serverversion", "day", "night"};
		for (String s2 : astring1) {
			if (s2.equals(s1)) {
				if (Bukkit.getServer().getPluginCommand(s2) != null) {
					String pName = Bukkit.getServer().getPluginCommand(s2).getPlugin().getDescription().getFullName();
					a.info("Command '" + s2 + "' conflicts with plugin '" + pName + "', McMyAdmin will not receive this command. Command will be sent to '" + pName + "' instead.");
					break;
				}
				if (playercommandpreprocessevent.getPlayer().isOp()) {
					a.info(playercommandpreprocessevent.getPlayer().getName() + " issued server command: " + s);
				} else {
					a.info(playercommandpreprocessevent.getPlayer().getName() + " tried command: " + s);
				}
				playercommandpreprocessevent.setCancelled(true);
				break;
			}
		}
	}
}
