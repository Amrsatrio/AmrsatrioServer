package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.EnumChatFormat;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class CommandEval extends AbstractBrigadierCommand {
	private static final ScriptEngine JS_ENGINE = new ScriptEngineManager().getEngineByName("js");

	public CommandEval() {
		super("eval", Messages.CMD_EVAL_DESC);
	}

	@Override
	public LiteralArgumentBuilder getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().requires(requireOp()).then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("code", StringArgumentType.greedyString()).executes(context -> handle(context.getSource(), StringArgumentType.getString(context, "code"))));
	}

	private static int handle(CommandListenerWrapper listener, String code) {
		listener.sendMessage(new ChatComponentText("<- " + code).a(EnumChatFormat.GRAY), false);

		try {
			listener.sendMessage(new ChatComponentText("-> " + JS_ENGINE.eval(code)), false);
		} catch (ScriptException e) {
			listener.sendMessage(new ChatComponentText(e.getMessage().replaceAll("\r", "\n")).a(EnumChatFormat.RED), false);
		}

		return Command.SINGLE_SUCCESS;
	}
}
