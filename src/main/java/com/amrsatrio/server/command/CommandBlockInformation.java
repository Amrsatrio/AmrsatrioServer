package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.util.RayTracer;
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
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IBlockState;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.IRegistry;
import net.minecraft.server.v1_14_R1.IVectorPosition;
import net.minecraft.server.v1_14_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.TileEntity;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//TODO: REFLECTION
public class CommandBlockInformation extends AbstractBrigadierCommand {
	private static final String ARG_POSITION = "position";
//	private static Function<Entry<IBlockState<?>, Comparable<?>>, String> stateToStringFunction = null;

//	static {
//		try {
//			Field field = BlockDataAbstract.class.getDeclaredField("b");
//			field.setAccessible(true);
//			stateToStringFunction = (Function<Entry<IBlockState<?>, Comparable<?>>, String>) field.get(null);
//		} catch (Throwable e) {
//			ServerPlugin.LOGGER.warn("Can't get BlockState function", e);
//		}
//	}

	public CommandBlockInformation() {
		super("blockinfo", Messages.CMD_BLOCKINFO_DESC);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		// @formatter:off
		return newRootNode()
				.requires(requireCheatsEnabled())
				.executes(context -> handle(context.getSource(), null))
				.then(RequiredArgumentBuilder.<CommandListenerWrapper, IVectorPosition>argument(ARG_POSITION, ArgumentPosition.a())
						.executes(context -> handle(context.getSource(), ArgumentPosition.a(context, ARG_POSITION)))
				);
		// @formatter:on
	}

	private static int handle(CommandListenerWrapper listener, BlockPosition position) throws CommandSyntaxException {
		if (position == null) {
			MovingObjectPositionBlock hitResult = RayTracer.rayTrace(listener.h().getBukkitEntity(), 5.0D);

			if (hitResult.d() && hitResult.getBlockPosition() != null) {
				position = hitResult.getBlockPosition();
			} else {
				throw Messages.POINTING_NOT_AT_BLOCK_ERROR.create();
			}
		}

		IBlockData iblockdata = listener.getWorld().getType(position);
		List<IChatBaseComponent> components = new ArrayList<>();
		components.add(Utils.title(new ChatMessage("Details of block at %s", Utils.blockPositionToComponent(position, false))));
		components.add(Utils.ddComponent("Name", new ChatComponentText(IRegistry.BLOCK.getKey(iblockdata.getBlock()).toString())));
//		components.add("Data value: " + ChatColor.RESET + iblockdata.getBlock().toLegacyData(iblockdata));
		Map<IBlockState<?>, Comparable<?>> stateMap = iblockdata.getStateMap();

		if (!stateMap.isEmpty()) {
			components.add(Utils.ddComponent("Block state", null));

			for (Entry<IBlockState<?>, Comparable<?>> map$entry : stateMap.entrySet()) {
				String stateValue = map$entry.getValue().toString();

				if (map$entry.getValue() == Boolean.TRUE) {
					stateValue = ChatColor.GREEN + stateValue;
				} else if (map$entry.getValue() == Boolean.FALSE) {
					stateValue = ChatColor.RED + stateValue;
				}

				components.add(Utils.ddComponent(" · " + map$entry.getKey().a(), new ChatComponentText(stateValue)));
			}
		}

		if (iblockdata.getBlock().isTileEntity()) {
			TileEntity tileEntity = listener.getWorld().getTileEntity(position);

			if (tileEntity != null) {
				components.add(Utils.ddComponent("Block entity data", new ChatComponentText(tileEntity.save(new NBTTagCompound()).toString())));
			}
		}

		String setBlockCommand = String.format("/setblock %d %d %d %s", position.getX(), position.getY(), position.getZ(), iblockdata.toString());

		if (listener.getEntity() instanceof EntityPlayer) {
			components.add(Utils.ddComponent("Actions", Utils.suggestBoxJson("setblock", setBlockCommand, false)));
		} else {
			components.add(Utils.ddComponent("Set block command", new ChatComponentText(setBlockCommand)));
		}

		components.forEach(iChatBaseComponent -> listener.sendMessage(iChatBaseComponent, false));
		return Command.SINGLE_SUCCESS;
	}
}
