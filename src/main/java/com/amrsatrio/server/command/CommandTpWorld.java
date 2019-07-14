package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.ICompletionProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class CommandTpWorld extends AbstractBrigadierCommand {
	private static final Predicate<String> VALID_WORLD_NAME_PREDICATE = s -> s.chars().allMatch(value -> value == 95 || value == 45 || value >= 97 && value <= 122 || value >= 48 && value <= 57 || value == 47 || value == 46);

	public CommandTpWorld() {
		super("tpworld", Messages.CMD_TPWORLD_DESC, Collections.singletonList("tpw"));
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().executes(context -> {
			List<String> list = Utils.getExistingWorlds();
			printWorldList(context.getSource(), list);
			return Command.SINGLE_SUCCESS;
		}).then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("world", StringArgumentType.word()).executes(context -> {
			CommandListenerWrapper listener = context.getSource();
			Player player = listener.h().getBukkitEntity();
			List<String> list = Utils.getExistingWorlds();
			String providedWorldName = parseWorldStringOrIndex(list, StringArgumentType.getString(context, "world"));
			IChatBaseComponent worldComponent = new ChatComponentText(providedWorldName);

			if (!list.contains(providedWorldName)) {
				listener.sendMessage(new ChatMessage(Messages.Commands.TPW_NONEXISTENT_TITLE, worldComponent), false);
				printWorldList(listener, list);
				return Command.SINGLE_SUCCESS;
			}

			World providedWorld = Bukkit.getWorld(providedWorldName);

			if (providedWorld != null) {
				worldComponent = Utils.worldToTextComponent(providedWorld);
			}

			if (player.getWorld() == providedWorld) {
				throw Messages.NOT_IN_WORLD_ERROR.create(worldComponent);
			}

			listener.sendMessage(new ChatMessage(Messages.Commands.TPW_TELEPORTING, worldComponent), false);

			if (providedWorld == null) {
				// TODO this halts the main thread
				Bukkit.getWorlds().add(providedWorld = Bukkit.createWorld(new WorldCreator(providedWorldName)));
			}

			// TODO make the player tp'd to its last position in that world instead of to the spawnpoint
			player.teleport(new Location(providedWorld, providedWorld.getSpawnLocation().getX(), providedWorld.getSpawnLocation().getY(), providedWorld.getSpawnLocation().getZ()));
//			listener.sendMessage(new ChatMessage("WARNING: This feature is very experimental. The scoreboard in %s are mixed with the scoreboard in %s. So don't try to teleport to lots-of-command-blocks maps!", worldBefore.getName(), providedWorld.getName()), false);
			listener.sendMessage(new ChatMessage(Messages.Commands.TPW_SUCCESS, listener.getName(), worldComponent), true);
			return Command.SINGLE_SUCCESS;
		}).suggests((commandContext, suggestionsBuilder) -> ICompletionProvider.b(Utils.getExistingWorlds().stream().filter(VALID_WORLD_NAME_PREDICATE), suggestionsBuilder)));
	}

	private static String parseWorldStringOrIndex(List<String> list, String argValue) throws CommandSyntaxException {
		argValue = argValue.trim();

		try {
			int raw = Integer.valueOf(argValue);

			if (raw < 1) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().create(0, raw);
			} else if (raw > list.size()) {
				throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().create(list.size(), raw);
			}

			return list.get(raw - 1);
		} catch (NumberFormatException e) {
			return argValue;
		}
	}

	private static void printWorldList(CommandListenerWrapper listener, List<String> list) {
		listener.sendMessage(Utils.title(new ChatComponentText("World List")), false);
		Utils.printListNumbered(listener.getBukkitSender(), list);
	}
}
