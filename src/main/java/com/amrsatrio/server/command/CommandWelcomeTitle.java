package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.EntityPlayer;

import org.bukkit.configuration.Configuration;

public class CommandWelcomeTitle extends AbstractBrigadierCommand {
	public CommandWelcomeTitle() {
		super("welcometitle", Messages.CMD_WELCOMETITLE_DESC);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		// @formatter:off
		return newRootNode()
				.requires(requireOp())
				.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("set")
						.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("title")
								.executes(context -> handleSet(context.getSource(), false, null))
								.then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("text", StringArgumentType.greedyString())
										.executes(context -> handleSet(context.getSource(), false, StringArgumentType.getString(context, "text")))
								)
						)
						.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("subtitle")
								.executes(context -> handleSet(context.getSource(), true, null))
								.then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("text", StringArgumentType.greedyString())
										.executes(context -> handleSet(context.getSource(), true, StringArgumentType.getString(context, "text")))
								)
						)
						.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("enable")
								.then(RequiredArgumentBuilder.<CommandListenerWrapper, Boolean>argument("state?", BoolArgumentType.bool())
										.executes(context -> handleState(context.getSource(), BoolArgumentType.getBool(context, "state?")))
								)
						)
				)
				.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("show")
						.then(RequiredArgumentBuilder.<CommandListenerWrapper, Boolean>argument("inChat?", BoolArgumentType.bool())
								.executes(context -> handleShow(context.getSource(), BoolArgumentType.getBool(context, "inChat?")))
						)
				);
		// @formatter:on
	}

	private static int handleState(CommandListenerWrapper listener, boolean enabled) throws CommandSyntaxException {
		ServerPlugin instance = ServerPlugin.getInstance();
		boolean state = instance.getConfig().getBoolean("welcome-state");

		if (enabled == state) {
			throw Messages.NOTHING_CHANGED_ERROR.create();
		}

		instance.getConfig().set("welcome-state", enabled);
		instance.saveConfig();
		listener.sendMessage(new ChatComponentText("Show welcome title to joined players: " + Utils.disabledOrEnabled(enabled)), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int handleShow(CommandListenerWrapper listener, boolean inChat) throws CommandSyntaxException {
		ServerPlugin instance = ServerPlugin.getInstance();
		Configuration config = instance.getConfig();

		if (!config.getBoolean("welcome-state")) {
			throw Messages.NOT_ENABLED_ERROR.create();
		}

		if (!(listener.getEntity() instanceof EntityPlayer) || inChat) {
			listener.sendMessage(new ChatComponentText("Welcome title message: \n" + config.getString("welcome-title").replaceAll("&", "\u00a7") + "\n" + config.getString("welcome-subtitle").replaceAll("&", "\u00a7")), false);
			return Command.SINGLE_SUCCESS;
		}

		instance.welcomeTitle(listener.h().getBukkitEntity());
		listener.sendMessage(new ChatComponentText("Welcome title displayed."), false);
		return Command.SINGLE_SUCCESS;
	}

	//	private static int handleSet(CommandListenerWrapper listener, boolean isSubtitle, IChatBaseComponent text) {
	private static int handleSet(CommandListenerWrapper listener, boolean isSubtitle, String text) {
		ServerPlugin instance = ServerPlugin.getInstance();
		String configKeyToSet = isSubtitle ? "welcome-subtitle" : "welcome-title";

		if (text == null || text.isEmpty()) {
			instance.getConfig().set(configKeyToSet, "");
			listener.sendMessage(new ChatComponentText(isSubtitle ? "Cleared the welcome title subtitle text." : "Cleared the welcome title title text."), true);
			return Command.SINGLE_SUCCESS;
		}

		instance.getConfig().set(configKeyToSet, text);
		instance.saveConfig();
		listener.sendMessage(new ChatComponentText((isSubtitle ? "Saved the welcome title subtitle text." : "Saved the welcome title title text.") + ' ' + text.replaceAll("&", "\u00a7")), true);
		return Command.SINGLE_SUCCESS;
	}
}
