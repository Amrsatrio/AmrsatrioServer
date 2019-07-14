package com.amrsatrio.server.bukkitcompat;

import com.amrsatrio.server.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.PrintStream;

@SuppressWarnings("deprecation")
public class Executor implements CommandExecutor {
	private PrintStream errout;

	public Executor(PrintStream a) {
		errout = a;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player p = null;
		String CommandName = command.getName().toLowerCase();
		String SourceName;
		boolean isServerCommand = !(sender instanceof Player);
		if (isServerCommand) {
			SourceName = "CONSOLE";
		} else {
			p = (Player) sender;
			SourceName = p.getName();
		}
		switch (CommandName) {
			case "tell": {
				if (split.length == 0) {
					sender.sendMessage(ChatColor.GRAY + "Syntax: /tell <target> <message>");
					return true;
				}
				String Target = split[0];
				String Message = Utils.buildString(split, 1);
				Player TargetPlayer = Bukkit.getServer().getPlayer(Target);
				if (TargetPlayer == null) {
					sender.sendMessage(ChatColor.GRAY + "There's no player by that name online");
				} else {
					if (isServerCommand) {
						TargetPlayer.sendMessage(ChatColor.AQUA + Message);
					} else {
						TargetPlayer.sendMessage(ChatColor.GRAY + SourceName + " whispers " + Message);
					}

				}
				break;
			}
			case "kickreason":
				if (!isServerCommand && !p.isOp()) {
					sender.sendMessage(ChatColor.GRAY + "You need to be an op to do that.");
					return false;
				}
				if (split.length == 0) {
					sender.sendMessage(ChatColor.GRAY + "Syntax: /kickreason <target> <reason>");
					return true;
				}
				String Target = split[0];
				String Message = Utils.buildString(split, 1);
				Player TargetPlayer = Bukkit.getServer().getPlayer(Target);
				if (TargetPlayer == null) {
					sender.sendMessage(ChatColor.GRAY + "There's no player by that name online");
				} else {
					TargetPlayer.kickPlayer(Message);
				}
				break;
			case "svping":
				if (isServerCommand) {
					errout.println("0 0 [MCMAX] pong " + Utils.buildString(split, 0));
				} else {
					sender.sendMessage(ChatColor.AQUA + "pong " + Utils.buildString(split, 0));
				}
				break;
			case "pushcommand":
				System.out.println(SourceName + " tried command: " + Utils.buildString(split, 0));
				break;
		}
		return true;
	}
}
