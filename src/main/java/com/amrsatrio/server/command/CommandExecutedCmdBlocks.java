package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatClickable;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatHoverable;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandBlockListenerAbstract;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.craftbukkit.v1_14_R1.command.CraftBlockCommandSender;

import java.util.List;

public class CommandExecutedCmdBlocks extends AbstractBrigadierCommand {
	public CommandExecutedCmdBlocks() {
		super("executedcmdblocks", Messages.CMD_EXECUTEDCMDBLOCKS_DESC);
	}

	@Override
	public LiteralArgumentBuilder<CommandListenerWrapper> getCommandNodeForRegistration(CommandDispatcher<CommandListenerWrapper> dispatcher) {
		return newRootNode().requires(requireCheatsEnabled()).executes(context -> {
			List<BlockCommandSender> cmdBlockList = ServerPlugin.getInstance().cmdBlockList;

			if (cmdBlockList.isEmpty()) {
				context.getSource().sendMessage(new ChatComponentText("No executed command blocks since the last 5 ticks"), false);
				return Command.SINGLE_SUCCESS;
			} else {
				for (int i = 0; i < cmdBlockList.size(); i++) {
					CommandListenerWrapper wrapper = ((CraftBlockCommandSender) cmdBlockList.get(i)).getWrapper();
//			CommandBlockListenerAbstract cmdBlockListener = (CommandBlockListenerAbstract) wrapper.base;
					BlockPosition bp = new BlockPosition(wrapper.getPosition());
					IChatBaseComponent worldComponent = new ChatComponentText("(W)");
					String worldName = wrapper.getWorld().getWorldData().getName();
					worldComponent.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, new ChatComponentText(worldName))).setChatClickable(new ChatClickable(ChatClickable.EnumClickAction.SUGGEST_COMMAND, "/tpw " + worldName));
					context.getSource().sendMessage(new ChatMessage("%s. [%s]%s%s %s", i + 1, wrapper.getName(), worldComponent, Utils.blockPositionToComponent(bp, false), truncate(((CommandBlockListenerAbstract) wrapper.base).getCommand(), 20)), false);
				}
			}

			return Command.SINGLE_SUCCESS;
		});
	}

	private static String truncate(String s, int length) {
		if (s.length() <= length) {
			return s;
		} else {
			return s.substring(0, length) + "...";
		}
	}
}
