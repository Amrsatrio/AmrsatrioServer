package com.amrsatrio.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.server.v1_14_R1.ArgumentEntity;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntitySelector;

import java.util.ArrayList;
import java.util.List;

public class CommandEvalSelector extends AbstractBrigadierCommand {
	private static final String ARG_EXPRESSION = "expression";
	private static final String MSG_PLURAL = "Found %d entities";
	private static final String MSG_SINGULAR = "Found %s";

	public CommandEvalSelector() {
		super("evalselector", null);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		// ArgumentEntity.b(): allows multiple entities
		return newRootNode().requires(requireCheatsEnabled()).then(RequiredArgumentBuilder.<CommandListenerWrapper, EntitySelector>argument(ARG_EXPRESSION, ArgumentEntity.c()).executes(context -> {
			CommandListenerWrapper sender = context.getSource();
			// Parse the entity argument into a list of entities, throw an error if entity matches none
			List<? extends Entity> list = new ArrayList<>(ArgumentEntity.b(context, ARG_EXPRESSION));
			int size = list.size();
//			String s = context.getArgument(ARG_EXPRESSION, String.class);
			sender.sendMessage(size == 1 ? new ChatMessage(MSG_SINGULAR, list.get(0).getDisplayName()) : new ChatMessage(MSG_PLURAL, size), false);
			return list.size();
			//TODO not working if size>1
		}));
	}
}
