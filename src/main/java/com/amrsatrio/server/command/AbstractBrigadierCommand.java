package com.amrsatrio.server.command;

import com.amrsatrio.server.ServerPlugin;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.v1_14_R1.CommandDispatcher;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_14_R1.command.VanillaCommandWrapper;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractBrigadierCommand {
	private String name;
	private String description;
	private List<String> aliases;
	private String permission;

	public AbstractBrigadierCommand(String name, String description) {
		this(name, description, Collections.emptyList());
	}

	public AbstractBrigadierCommand(String name, String description, List<String> aliases) {
		this.name = name;
		this.description = description;
		this.aliases = aliases;
	}

	public boolean registerToCommandMap(CommandMap bukkitRegistry, CommandDispatcher minecraftRegistry) {
		com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> bgDispatcher = minecraftRegistry.a();
		LiteralCommandNode<CommandListenerWrapper> node = bgDispatcher.register(getCommandNodeForRegistration(bgDispatcher));
		VanillaCommandWrapper bktCommand = new VanillaCommandWrapper(minecraftRegistry, node);

		for (String label : aliases) {
			bgDispatcher.register(LiteralArgumentBuilder.<CommandListenerWrapper>literal(label).redirect(node));
		}

		bktCommand.setAliases(aliases);
		bktCommand.setPermission(permission);
		String name = ServerPlugin.getInstance().getName();
		bktCommand.setDescription(description == null ? name + " plugin command" : description);
//		bktCommand.setUsage(dispatcher.a().getSmartUsage(command, ((CraftServer) Bukkit.getServer()).getServer().getServerCommandListener()));
		return bukkitRegistry.register(name, bktCommand);
	}

	public abstract LiteralArgumentBuilder getCommandNodeForRegistration(com.mojang.brigadier.CommandDispatcher<CommandListenerWrapper> dispatcher);

	protected final LiteralArgumentBuilder<CommandListenerWrapper> newRootNode() {
		return LiteralArgumentBuilder.literal(name);
	}

	protected final Predicate<CommandListenerWrapper> requireCheatsEnabled() {
		return source -> source.hasPermission(2);
	}

	protected final Predicate<CommandListenerWrapper> requireOp() {
		return source -> source.hasPermission(3);
	}
}
