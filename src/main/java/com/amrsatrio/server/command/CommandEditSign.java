package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.util.RayTracer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_14_R1.ArgumentPosition;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.IVectorPosition;
import net.minecraft.server.v1_14_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenSignEditor;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.TileEntitySign;

public class CommandEditSign extends AbstractBrigadierCommand {
	private static final String ARG_POS = "position";

	public CommandEditSign() {
		super("editsign", Messages.CMD_EDITSIGN_DESC);
	}

	@Override
	public LiteralArgumentBuilder getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().executes(context -> handle(context.getSource(), null)).then(RequiredArgumentBuilder.<CommandListenerWrapper, IVectorPosition>argument(ARG_POS, ArgumentPosition.a()).executes(context -> handle(context.getSource(), ArgumentPosition.a(context, ARG_POS))));
	}

	private static int handle(CommandListenerWrapper listener, BlockPosition pos) throws CommandSyntaxException {
		EntityPlayer entityplayer = listener.h();

		if (pos == null) {
			MovingObjectPositionBlock hitResult = RayTracer.rayTrace(entityplayer.getBukkitEntity(), 5.0D);

			if (hitResult.d()) {
				pos = hitResult.getBlockPosition();
			} else {
				throw Messages.POINTING_NOT_AT_BLOCK_ERROR.create();
			}
		}

		TileEntity tileentity = entityplayer.getWorld().getTileEntity(pos);

		if (!(tileentity instanceof TileEntitySign)) {
			throw Messages.NOT_A_SIGN_ERROR.create(pos);
		}

		TileEntitySign tileentitysign = (TileEntitySign) tileentity;
		tileentitysign.isEditable = true;
		tileentitysign.a((EntityHuman) entityplayer);
		entityplayer.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(pos));
		return Command.SINGLE_SUCCESS;
	}
}
