package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.ServerPlugin;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.v1_14_R1.CommandListenerWrapper;

public class CommandConfig extends AbstractBrigadierCommand {
	private static final String ARG_KEY = "key", ARG_VALUE = "value";

	public CommandConfig() {
		super("sconfig", Messages.CMD_SCONFIG_DESC);
	}

	@Override
	public LiteralArgumentBuilder getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().requires(requireOp()).then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("get").then(keyArg().executes(context -> handle(context.getSource(), StringArgumentType.getString(context, ARG_KEY), false, false)))).then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("set").then(keyArg().then(RequiredArgumentBuilder.<CommandListenerWrapper, Boolean>argument(ARG_VALUE, BoolArgumentType.bool()).executes(context -> handle(context.getSource(), StringArgumentType.getString(context, ARG_KEY), true, BoolArgumentType.getBool(context, ARG_VALUE))))));
	}

	private static RequiredArgumentBuilder<CommandListenerWrapper, String> keyArg() {
		return RequiredArgumentBuilder.<CommandListenerWrapper, String>argument(ARG_KEY, StringArgumentType.word()).suggests((context, builder) -> {
			for (String key : ServerPlugin.getInstance().configManager.getKeys()) {
				builder.suggest(key);
			}

			return builder.buildFuture();
		});
	}

	private static int handle(CommandListenerWrapper source, String key, boolean set, boolean setValue) throws CommandSyntaxException {
		return ServerPlugin.getInstance().configManager.executeInCommand(source, key, set, setValue);
	}
}
