package com.amrsatrio.server.bukkitcompat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.logging.Logger;

public class BukkitCompat implements Listener {
	private Logger log;

	public BukkitCompat() {
		this.log = Logger.getLogger("Minecraft");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String Message = event.getMessage().substring(1);
		String[] CommandAndArgs = Message.split(" ", 2);
		String Command = CommandAndArgs[0];
		/*
		 * String[] IgnoreCommands = { "accept", "addmember", "backupworld",
		 * "decline", "delmember", "delplayerdata", "giveme", "giveto", "goto",
		 * "invite", "join", "lastseen", "lookup", "motd", "opme", "players",
		 * "restart", "serverstats", "userstats", "whois", "creative",
		 * "survival", "serverversion", "day", "night" };
		 */
		String[] IgnoreCommands = {"accept", "addmember", "backupworld", "decline", "delmember", "delplayerdata", "giveme", "giveto", "goto", "invite", "join", "lastseen", "lookup", "motd", "opme", "players", "serverstats", "userstats", "whois", "creative", "survival", "serverversion", "day", "night"};
		for (String S : IgnoreCommands) {
			if (S.equals(Command)) {
				if (Bukkit.getServer().getPluginCommand(S) != null) {
					String pName = Bukkit.getServer().getPluginCommand(S).getPlugin().getDescription().getFullName();
					this.log.info("Command '" + S + "' conflicts with plugin '" + pName + "', McMyAdmin will not receive this command. Command will be sent to '" + pName + "' instead.");
					break;
				}
				if (event.getPlayer().isOp()) {
					this.log.info(event.getPlayer().getName() + " issued server command: " + Message);
				} else {
					this.log.info(event.getPlayer().getName() + " tried command: " + Message);
				}
				event.setCancelled(true);
				break;
			}
		}
	}
}
