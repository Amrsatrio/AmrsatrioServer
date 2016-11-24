package com.amrsatrio.server;

import org.apache.commons.cli.*;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.amrsatrio.server.AmrsatrioServer.*;

public class CommandBanAdvanced implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender commandsender, Command command, String s, String[] astring) {
		validateOp(commandsender);
		Options options = new Options().addOption(Option.builder("p").desc("Player name/UUID to ban").hasArg().argName("player").required().build()).addOption(Option.builder("t").desc("How long will the player be banned").hasArg().argName("time").build()).addOption(Option.builder("r").desc("Reason of ban").hasArg().argName("reason").build());

		CommandLine commandline;
		try {
			commandline = new DefaultParser().parse(options, astring);
		} catch (ParseException e) {
			new HelpFormatter().printHelp("/banadv", options, true);
			return true;
//			throw new CommandException("Error parsing command", e);
		}

		Matcher matcher = Pattern.compile("\\d+\\s\\w+|in(de|)finite").matcher(commandline.getOptionValue('t', "indefinite"));
		List<String> list = new ArrayList<>();

		while (matcher.find()) {
			list.add(matcher.group());
		}

		Date date;

		if (list.contains("infinite") || list.contains("indefinite")) {
			date = null;
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(System.currentTimeMillis()));

			for (String s1 : list) {
				String[] astring1 = s1.split(" ");
				int i = Integer.parseInt(astring1[0]);
				String s2 = astring1[1].toLowerCase();

				if (s2.endsWith("s")) {
					s2 = s2.substring(0, s2.length() - 1);
				}

				switch (s2) {
					case "second":
						calendar.add(Calendar.SECOND, i);
						break;

					case "minute":
						calendar.add(Calendar.MINUTE, i);
						break;

					case "hour":
						calendar.add(Calendar.HOUR, i);
						break;

					case "day":
						calendar.add(Calendar.DATE, i);
						break;

					case "week":
						calendar.add(Calendar.WEEK_OF_YEAR, i);
						break;

					case "month":
						calendar.add(Calendar.MONTH, i);
						break;

					case "year":
						calendar.add(Calendar.YEAR, i);
						break;

					default:
						throw new CommandException("What is " + s2 + "? I don't know.");
				}
			}

			date = calendar.getTime();
		}

		String s3 = commandline.getOptionValue('p');
		String s4 = commandline.getOptionValue("r");
		Bukkit.getBanList(BanList.Type.NAME).addBan(s3, s4, date, null);
		Player player = Bukkit.getServer().getPlayer(s3);

		if (player != null) {
			player.kickPlayer("You are banned from this server.");
		}

		msg(commandsender, "Banned player " + s3 + " " + (date == null ? "forever" : "until " + SDF.format(date)) + " with reason " + s4);
		return true;
	}
}
