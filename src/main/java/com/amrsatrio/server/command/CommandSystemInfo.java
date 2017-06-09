package com.amrsatrio.server.command;

import net.minecraft.server.v1_12_R1.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;

import java.util.Collections;

public class CommandSystemInfo extends CustomizedPluginCommand {
	private static final ChatColor MSG_COLOR = ChatColor.GRAY;

	public CommandSystemInfo() {
		super("systeminfo", "Display system info. Sometimes it might not work.", "/<command>", Collections.singletonList("systeminfo"));
		setPermission("amrsatrioserver.sysinfo");
	}

	@Override
	public boolean executePluginCommand(CommandSender commandsender, String s, String[] astring) throws CommandException {
		if (commandsender instanceof BlockCommandSender) {
			throw new CommandException("Command blocks are not allowed to execute this command");
		}

//		commandsender.sendMessage(ChatColor.DARK_GREEN + "--- System information ---");
//		SystemInfo sys = new SystemInfo();
//		HardwareAbstractionLayer hal = sys.getHardware();
//		commandsender.sendMessage(MSG_COLOR + "Processor: " + ChatColor.RESET + hal.getProcessor().getLogicalProcessorCount() + "x " + hal.getProcessor().getName());
//		commandsender.sendMessage(MSG_COLOR + "RAM: " + ChatColor.RESET + freeOf(hal.getMemory().getAvailable(), hal.getMemory().getTotal()));
//		OperatingSystem os = sys.getOperatingSystem();
//		commandsender.sendMessage(MSG_COLOR + "Operating system: " + ChatColor.RESET);
//		commandsender.sendMessage(MSG_COLOR + " Name: " + ChatColor.RESET + os.getManufacturer() + " " + os.getFamily());
//		commandsender.sendMessage(MSG_COLOR + " Version: " + ChatColor.RESET + os.getVersion());
//		commandsender.sendMessage(MSG_COLOR + "Drives:");
//
//		for (OSFileStore f : sys.getHardware().getFileStores()) {
//			if (!f.getName().isEmpty()) {
//				commandsender.sendMessage(MSG_COLOR + " " + f.getName() + ": " + ChatColor.RESET + freeOf(f.getUsableSpace(), f.getTotalSpace()));
//			}
//		}
		throw new CommandException("I'm gonna split this command to another plugin");
//		return true;
	}
}
