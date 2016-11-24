package com.amrsatrio.server.bukkitcompat;

import java.io.PrintStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.amrsatrio.server.Utils;

@SuppressWarnings("deprecation")
public class Executor implements CommandExecutor {
	private PrintStream errout;

	public Executor(PrintStream a) {
		this.errout = a;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		Player p = null;
		String CommandName = command.getName().toLowerCase();
		String SourceName = "";
		boolean isServerCommand = !(sender instanceof Player);
		if (isServerCommand) SourceName = "CONSOLE";
		else {
			p = (Player) sender;
			SourceName = p.getName();
		}
		if (CommandName.equals("tell")) {
			if (split.length == 0) {
				sender.sendMessage(ChatColor.GRAY + "Syntax: /tell <target> <message>");
				return true;
			}
			String Target = split[0];
			String Message = Utils.buildString(split, 1);
			Player TargetPlayer = Bukkit.getServer().getPlayer(Target);
			if (TargetPlayer == null) sender.sendMessage(ChatColor.GRAY + "There's no player by that name online");
			else {
				if (isServerCommand) TargetPlayer.sendMessage(ChatColor.AQUA + Message);
				else TargetPlayer.sendMessage(ChatColor.GRAY + SourceName + " whispers " + Message);

			}
		} else if (CommandName.equals("kickreason")) {
			if ((!isServerCommand) && (!p.isOp())) {
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
			if (TargetPlayer == null) sender.sendMessage(ChatColor.GRAY + "There's no player by that name online");
			else TargetPlayer.kickPlayer(Message);
		} else if (CommandName.equals("svping")) {
			if (isServerCommand) this.errout.println("0 0 [MCMAX] pong " + Utils.buildString(split, 0));
			else sender.sendMessage(ChatColor.AQUA + "pong " + Utils.buildString(split, 0));
		} else if (CommandName.equals("pushcommand"))
			System.out.println(SourceName + " tried command: " + Utils.buildString(split, 0));
		return true;
	}
}
