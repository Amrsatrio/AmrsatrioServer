package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandException;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.EntityItem;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.ItemWorldMap;
import net.minecraft.server.v1_14_R1.MapIcon;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldMap;

import java.util.Locale;
import java.util.Random;

public class CommandGetExplorerMap extends AbstractBrigadierCommand {
	private static final Random RANDOM = new Random();

	public CommandGetExplorerMap() {
		super("getexplorermap", Messages.CMD_GETEXPLORERMAP_DESC);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().requires(requireCheatsEnabled()).executes(context -> execute(context.getSource(), null)).then(RequiredArgumentBuilder.<CommandListenerWrapper, String>argument("type", StringArgumentType.word()).suggests((context, builder) -> builder.suggest("Mansion").suggest("Monument").buildFuture()).executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "type"))));
	}

	private static int execute(CommandListenerWrapper nmsSender, String feature) throws CommandException, CommandSyntaxException {
		EntityPlayer targetPlayer = nmsSender.h();

		if (feature == null) {
			int i = RANDOM.nextInt(2);
			feature = new String[]{"Mansion", "Monument"}[i];
		}

		MapIcon.Type structureType;

		switch (feature) {
			case "Mansion":
				structureType = MapIcon.Type.MANSION;
				break;
			case "Monument":
				structureType = MapIcon.Type.MONUMENT;
				break;
			default:
				throw Messages.UNSUPPORTED_STRUCTURE_TYPE_ERROR.create();
		}

		if (!targetPlayer.isCreative()) {
			throw Messages.NOT_IN_CREATIVE_ERROR.create();
		}

		World world = nmsSender.getWorld();
		BlockPosition structurePosition = world.a(feature, new BlockPosition(nmsSender.getPosition()), 100, true);

		if (structurePosition != null) {
			ItemStack resultItem = ItemWorldMap.createFilledMapView(world, structurePosition.getX(), structurePosition.getZ(), (byte) 2, true, true);
			ItemWorldMap.applySepiaFilter(world, resultItem);
			WorldMap.decorateMap(resultItem, structurePosition, "+", structureType);
			ChatMessage itemName = new ChatMessage("filled_map." + feature.toLowerCase(Locale.ROOT));
			resultItem.a(itemName);
			EntityItem itemDrop = targetPlayer.drop(resultItem, false);

			if (itemDrop != null) {
				itemDrop.o();
				itemDrop.setOwner(targetPlayer.getUniqueID());
			}

//			Bukkit.dispatchCommand(bukkitSender, "give " + bukkitSender.getName() + " minecraft:filled_map 1 " + resultItem.getData() + ' ' + resultItem.getTag());
			nmsSender.sendMessage(new ChatMessage("Successfully created %s", itemName), true);
			return Command.SINGLE_SUCCESS;
		} else {
			throw Messages.STRUCTURE_NOT_FOUND_ERROR.create();
		}
	}
}
