package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.ServerPlugin;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_14_R1.ArgumentChat;
import net.minecraft.server.v1_14_R1.ArgumentProfile;
import net.minecraft.server.v1_14_R1.ChatComponentUtils;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.GameProfileBanEntry;
import net.minecraft.server.v1_14_R1.GameProfileBanList;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandBanExtended extends AbstractBrigadierCommand {
	public CommandBanExtended() {
		super("banx", Messages.CMD_BANX_DESC);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().requires(requireOp().and(context -> context.getServer().getPlayerList().getProfileBans().isEnabled())).then(RequiredArgumentBuilder.<CommandListenerWrapper, ArgumentProfile.a>argument("targets", ArgumentProfile.a()).executes(context -> execute(context.getSource(), ArgumentProfile.a(context, "targets"), null, null)).then(RequiredArgumentBuilder.<CommandListenerWrapper, ArgumentChat.a>argument("reason", ArgumentChat.a()).executes(context -> execute(context.getSource(), ArgumentProfile.a(context, "targets"), ArgumentChat.a(context, "reason"), null)).then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("duration", StringArgumentType.string()).executes(context -> execute(context.getSource(), ArgumentProfile.a(context, "targets"), ArgumentChat.a(context, "reason"), StringArgumentType.getString(context, "duration"))))));
	}

	private static int execute(CommandListenerWrapper listener, Collection<GameProfile> targets, @Nullable IChatBaseComponent reason, @Nullable String duration) throws CommandSyntaxException {
		GameProfileBanList banList = listener.getServer().getPlayerList().getProfileBans();
		int bannedCount = 0;

		for (GameProfile profile : targets) {
			if (!banList.isBanned(profile)) {
				Date expiryDate = duration == null ? null : stringExpiryDate(duration);
				GameProfileBanEntry banEntry = new GameProfileBanEntry(profile, null, listener.getName(), expiryDate, reason == null ? null : reason.getString());
				banList.add(banEntry);
				++bannedCount;
				listener.sendMessage(new ChatMessage("commands.ban.success", ChatComponentUtils.a(profile), banEntry.getReason()), true);

				if (expiryDate != null) {
					listener.sendMessage(new ChatMessage("Until %s", ServerPlugin.SDF.format(expiryDate)), true);
				}

				EntityPlayer playerToDisconnect = listener.getServer().getPlayerList().a(profile.getId());

				if (playerToDisconnect != null) {
					playerToDisconnect.playerConnection.disconnect(new ChatMessage("multiplayer.disconnect.banned"));
				}
			}
		}

		if (bannedCount == 0) {
			throw Messages.BAN_FAILED_ERROR.create();
		} else {
			return bannedCount;
		}
	}

	private static Date stringExpiryDate(String input) throws CommandSyntaxException {
		Matcher matcher = Pattern.compile("\\d+\\s\\w+|in(de|)finite").matcher(input);
		List<String> nodes = new ArrayList<>();

		while (matcher.find()) {
			nodes.add(matcher.group());
		}

		Date date;

		if (nodes.contains("infinite") || nodes.contains("indefinite")) {
			date = null;
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(System.currentTimeMillis()));

			for (String node : nodes) {
				String[] split = node.split(" ");
				int timeValue = Integer.valueOf(split[0]);
				String timeUnitString = split[1].toLowerCase();

				if (timeUnitString.startsWith("mo")) {
					calendar.add(Calendar.MONTH, timeValue);
				} else if (timeUnitString.startsWith("m")) {
					calendar.add(Calendar.MINUTE, timeValue);
				} else if (timeUnitString.startsWith("h")) {
					calendar.add(Calendar.HOUR, timeValue);
				} else if (timeUnitString.startsWith("d")) {
					calendar.add(Calendar.DATE, timeValue);
				} else if (timeUnitString.startsWith("w")) {
					calendar.add(Calendar.WEEK_OF_YEAR, timeValue);
				} else if (timeUnitString.startsWith("s")) {
					calendar.add(Calendar.SECOND, timeValue);
				} else if (timeUnitString.startsWith("y")) {
					calendar.add(Calendar.YEAR, timeValue);
				} else {
					throw Messages.UNKNOWN_ERROR.create(timeUnitString);
				}
			}

			date = calendar.getTime();
		}

		return date;
	}
}
