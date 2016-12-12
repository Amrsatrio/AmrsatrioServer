package com.amrsatrio.server.command;

import com.amrsatrio.server.Messages;
import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.Utils;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.util.List;

public abstract class CustomizedPluginCommand extends Command implements PluginIdentifiableCommand {
	protected CustomizedPluginCommand(String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
		setPermissionMessage(Messages.Commands.NO_PERM);
	}

	protected static IChatBaseComponent createMessage(IChatBaseComponent msg, String tag) {
		return new ChatMessage(ServerPlugin.SH, tag).setChatModifier(new ChatModifier().setColor(EnumChatFormat.BLUE)).addSibling(new ChatComponentText("").addSibling(msg).setChatModifier(new ChatModifier().setColor(EnumChatFormat.GRAY)));
	}

	public String getUsage(String s) {
		return getUsage().replaceFirst("<command>", s);
	}

	@Override
	public final boolean execute(CommandSender commandSender, String s, String[] astring) {
		ICommandListener var1 = Utils.getListener(commandSender);
		ChatMessage chatmessage;

		try {
			if (!getPlugin().isEnabled()) {
				throw new CommandException(Messages.Commands.PLUGIN_DISABLED);
			}

			if (!testPermissionSilent(commandSender)) {
				throw new CommandException(getPermissionMessage());
			}

			return executePluginCommand(commandSender, var1, s, astring);
		} catch (ExceptionUsage var7) {
			chatmessage = new ChatMessage("commands.generic.usage", new ChatMessage(var7.getMessage(), var7.getArgs()));
		} catch (CommandException var8) {
			chatmessage = new ChatMessage(var8.getMessage(), var8.getArgs());
		} catch (Throwable var9) {
			chatmessage = new ChatMessage("commands.generic.exception");
			ServerPlugin.LOGGER.warn("Couldn't process {} in {}.", getClass().getSimpleName(), getPlugin().getName());
			var9.printStackTrace();
		}

		if (chatmessage != null) {
			chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
			var1.sendMessage(createMessage(chatmessage, "Error"));
		}

		return true;
	}

	public boolean executePluginCommand(CommandSender commandsender, ICommandListener icommandlistener, String s, String[] astring) throws CommandException {
		return executePluginCommand(commandsender, s, astring);
	}

	public boolean executePluginCommand(CommandSender commandsender, String s, String[] astring) throws CommandException {
		return false;
	}

	@Override
	public Plugin getPlugin() {
		return ServerPlugin.getInstance();
	}
}
