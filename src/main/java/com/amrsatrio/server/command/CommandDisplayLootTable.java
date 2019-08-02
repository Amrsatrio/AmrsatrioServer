package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.v1_14_R1.ArgumentMinecraftKeyRegistered;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.Container;
import net.minecraft.server.v1_14_R1.ContainerChest;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.ICompletionProvider;
import net.minecraft.server.v1_14_R1.IInventory;
import net.minecraft.server.v1_14_R1.InventorySubcontainer;
import net.minecraft.server.v1_14_R1.InventoryUtils;
import net.minecraft.server.v1_14_R1.LootContextParameters;
import net.minecraft.server.v1_14_R1.LootTable;
import net.minecraft.server.v1_14_R1.LootTableInfo;
import net.minecraft.server.v1_14_R1.LootTables;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_14_R1.World;
import net.minecraft.server.v1_14_R1.WorldServer;

public class CommandDisplayLootTable extends AbstractBrigadierCommand {
	public CommandDisplayLootTable() {
		super("loottable", Messages.CMD_LOOTTABLE_DESC);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().requires(requireCheatsEnabled()).then(RequiredArgumentBuilder.<CommandListenerWrapper, MinecraftKey>argument("loot table name", ArgumentMinecraftKeyRegistered.a()).suggests((context, builder) -> ICompletionProvider.a(LootTables.a(), builder)).executes(context -> {
			// loottable minecraft:some_thing
			return execute(context, ArgumentMinecraftKeyRegistered.c(context, "loot table name"), false);
		}).then(RequiredArgumentBuilder.<CommandListenerWrapper, Boolean>argument("drop?", BoolArgumentType.bool()).executes(context -> {
			// loottable minecraft:some_thing true
			return execute(context, ArgumentMinecraftKeyRegistered.c(context, "loot table name"), BoolArgumentType.getBool(context, "drop?"));
		})));
	}

	private static int execute(CommandContext<CommandListenerWrapper> context, MinecraftKey lootTableName, boolean drop) throws CommandSyntaxException {
		EntityPlayer player = context.getSource().h();

		if (!player.isCreative()) {
			throw Messages.NOT_IN_CREATIVE_ERROR.create();
		}

		IInventory lootTableInventory = getLootTableInventory(player.getWorld(), lootTableName);

		if (drop) {
			InventoryUtils.dropEntity(player.getWorld(), player, lootTableInventory);
		} else {
			int containerCounter = player.nextContainerCounter();
			Container container = ContainerChest.a(containerCounter, player.inventory, lootTableInventory);
			container.setTitle(new ChatComponentText(lootTableName.toString()));
			player.activeContainer = container;
			player.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, container.getType(), container.getTitle()));
			container.addSlotListener(player);
		}

		return Command.SINGLE_SUCCESS;
	}

//	public static Set<MinecraftKey> getAvailableLootTables(CommandListenerWrapper listener) {
//		if (!(listener instanceof Player)) {
//			return list;
//		}
//
//		File file = new File(new File(((Player) listener).getWorld().getWorldFolder(), "data"), "loot_tables");
//
//		if (!file.exists() || file.isFile()) {
//			return list;
//		}
//
//		for (File file1 : file.listFiles()) {
//			if (file1.isDirectory()) {
//				for (File file2 : file1.listFiles()) {
//					if (file2.isFile() && file2.getName().endsWith(".json")) {
//						list.add(new MinecraftKey(file1.getName(), file2.getName().substring(0, file2.getName().length() - ".json".length())));
//					}
//				}
//			}
//		}
//
//		return getLootTableRegistry(listener).a();
//	}

	private static IInventory getLootTableInventory(World world, MinecraftKey lootTableKey) throws CommandSyntaxException {
		LootTable loottable = world.getMinecraftServer().getLootTableRegistry().getLootTable(lootTableKey);

		if (loottable == LootTable.a) {
			throw Messages.INVALID_LOOT_TABLE_ERROR.create(lootTableKey);
		}

		IInventory iinventory = new InventorySubcontainer(3 * 9);
		loottable.fillInventory(iinventory, new LootTableInfo.Builder((WorldServer) world).set(LootContextParameters.POSITION, BlockPosition.ZERO).build(loottable.getLootContextParameterSet()));
		return iinventory;
	}
}
