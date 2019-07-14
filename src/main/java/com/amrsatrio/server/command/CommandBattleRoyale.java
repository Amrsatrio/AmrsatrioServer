package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.util.BattleRoyale;
import com.amrsatrio.server.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_14_R1.ArgumentPosition;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.IVectorPosition;
import net.minecraft.server.v1_14_R1.World;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

import java.util.ArrayList;
import java.util.List;

public class CommandBattleRoyale extends AbstractBrigadierCommand {
	public CommandBattleRoyale() {
		super("battleroyale", Messages.CMD_BATTLEROYALE_DESC);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		// @formatter:off
		// ArgumentPosition.a(CommandContext, String): get position checked
		// ArgumentPosition.b(CommandContext, String): get position direct
		return newRootNode()
				.requires(requireOp())
				.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("set")
						.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("world")
								.executes(context -> {
									World nmsWorld = context.getSource().h().getWorld();
									getBattleRoyale().setBukkitWorld(nmsWorld.getWorld());
									context.getSource().sendMessage(new ChatMessage("Set the battle royale world to %s", Utils.worldToTextComponent(nmsWorld)), true);
									return Command.SINGLE_SUCCESS;
								})
						)
						// TODO make the Y pos argument not required
						.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("pos1")
								.executes(context -> handleSetPos(context.getSource(), false, null))
								.then(RequiredArgumentBuilder.<CommandListenerWrapper, IVectorPosition>argument("position", ArgumentPosition.a())
										.executes(context -> handleSetPos(context.getSource(), false, ArgumentPosition.b(context, "position")))
								)
						)
						.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("pos2")
								.executes(context -> handleSetPos(context.getSource(), true, null))
								.then(RequiredArgumentBuilder.<CommandListenerWrapper, IVectorPosition>argument("position", ArgumentPosition.a())
										.executes(context -> handleSetPos(context.getSource(), true, ArgumentPosition.b(context, "position")))
								)
						)
				)
				.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("start")
						.executes(context -> handleState(context.getSource(), true))
				)
				.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("stop")
						.executes(context -> handleState(context.getSource(), false))
				)
				.then(LiteralArgumentBuilder.<CommandListenerWrapper>literal("query")
						.executes(context -> {
							List<IChatBaseComponent> components = new ArrayList<>();
							BattleRoyale battleRoyale = getBattleRoyale();
							components.add(Utils.title(new ChatComponentText("Battle Royale details")));
							org.bukkit.World world = battleRoyale.getAssignedWorld();
							components.add(Utils.ddComponent("World", world == null ? new ChatComponentText("(not set)") : Utils.worldToTextComponent(((CraftWorld) world).getHandle())));
							components.add(Utils.ddComponent("Region", new ChatMessage("%s - %s", Utils.blockPositionToComponent(battleRoyale.regionPosStart, true), Utils.blockPositionToComponent(battleRoyale.regionPosEnd, true))));
							components.add(Utils.ddComponent("State", new ChatComponentText(battleRoyale.isRunning() ? String.format("Running: Circle %s of %s", battleRoyale.getBlueZoneStage() + 1, battleRoyale.getTotalBlueZoneStages()) : "Not running")));

							for (IChatBaseComponent component : components) {
								context.getSource().sendMessage(component, false);
							}

							return Command.SINGLE_SUCCESS;
						})
				);
		// @formatter:on
	}

	private static int handleSetPos(CommandListenerWrapper listener, boolean isPos2, BlockPosition position) throws CommandSyntaxException {
		if (position == null) {
			position = listener.h().getChunkCoordinates();
		} else {
			BlockPosition.MutableBlockPosition mutable = new BlockPosition.MutableBlockPosition(position);
			mutable.p(0);
			position = mutable.immutableCopy();
		}

		BattleRoyale battleRoyale = getBattleRoyale();

		if (isPos2 ? battleRoyale.regionPosEnd == position : battleRoyale.regionPosStart == position) {
			throw Messages.NO_POSITION_CHANGE_ERROR.create();
		}

		if (isPos2) {
			battleRoyale.regionPosEnd = position;
		} else {
			battleRoyale.regionPosStart = position;
		}

		listener.sendMessage(new ChatMessage(isPos2 ? "Battle royale second position set to %s" : "Battle royale first position set to %s", Utils.blockPositionToComponent(position, true)), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int handleState(CommandListenerWrapper source, boolean newState) throws CommandSyntaxException {
		// TODO idk what the return value does
		BattleRoyale instance = getBattleRoyale();

		if (newState == instance.isRunning()) {
			throw (newState ? Messages.ALREADY_STARTED_ERROR : Messages.ALREADY_STOPPED_ERROR).create();
		}

		if (newState) {
			List<String> list = new ArrayList<>();
			Utils.checkNullity(instance.getAssignedWorld(), "world", list);
			Utils.checkNullity(instance.regionPosStart, "pos1", list);
			Utils.checkNullity(instance.regionPosEnd, "pos2", list);

			if (list.isEmpty()) {
				instance.start();
			} else {
				throw Messages.NOT_SET_ERROR.create(list);
			}
		} else {
			instance.stop();
		}

		source.sendMessage(new ChatComponentText(newState ? "Battle royale started" : "Battle royale stopped"), true);
		return Command.SINGLE_SUCCESS;
	}

	private static BattleRoyale getBattleRoyale() {
		return ServerPlugin.getInstance().battleRoyale;
	}
}
