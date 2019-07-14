package com.amrsatrio.server;

import com.amrsatrio.server.util.Utils;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatHoverable;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.EnumGamemode;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.List;
import java.util.stream.Collectors;

public class Messages {
	public static final String CMD_BLOCKINFO_DESC = "Displays the metadata of a block";
	public static final String CMD_EDITSIGN_DESC = "Edit the sign you're pointing or at the specified coordinates";
	public static final String CMD_BANX_DESC = "Same as \"/ban\" but with duration argument";
	public static final String CMD_BATTLEROYALE_DESC = "Runs a battle royale system on a world. This modifies the world's world border configuration. Experimental feature, use with caution.";
	public static final String CMD_SCONFIG_DESC = "Modify plugin configuration";
	public static final String CMD_LOOTTABLE_DESC = "View a loot table in a temporary container or drop all of its generated items";
	public static final String CMD_EVAL_DESC = "Evaluate a JavaScript code";
	public static final String CMD_EXECUTEDCMDBLOCKS_DESC = "Lists executed command blocks since the last 5 ticks";
	public static final String CMD_GETEXPLORERMAP_DESC = "Gives you an random explorer map like the one given by cartographer villagers. The \"type\" argument only accepts \"Mansion\" or \"Monument\", if not specified, it will default to either of those.";
	public static final String CMD_TPWORLD_DESC = "Teleports you to another world. Highly experimental.";
	public static final String CMD_WELCOMETITLE_DESC = "Interact with the welcome title system";
	public static final String MODDED_WARNING = "We have detected that you are using a modified version of Minecraft, which is \"%s\". You are allowed to use your modified client as long as you play fair!";
	public static final String UNSUPPORTED = "This plugin does not support your version of Minecraft Server! You have to contact me to update this plugin to support %1$s, or change the server software into version %2$s. Contact info: (IG: @not_armzyy)";
	public static final DynamicCommandExceptionType BLOCKED_BY_BLOCK_ERROR = new DynamicCommandExceptionType(o -> {
		ChatComponentText text = new ChatComponentText("Couldn't build the text because the resulting blocks are blocked by existing block(s).");
		text.getChatModifier().setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, new ChatComponentText(((List<Location>) o).stream().map(location -> "[" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]").collect(Collectors.joining(", ")))));
		return text;
	});
	public static final DynamicCommandExceptionType INVALID_LOOT_TABLE_ERROR = new DynamicCommandExceptionType(o -> new ChatComponentText("Non-existent loot table " + o));
	public static final DynamicCommandExceptionType NOT_A_SIGN_ERROR = new DynamicCommandExceptionType(o -> new ChatMessage("The block at %s is not a sign", o));
	public static final DynamicCommandExceptionType NOT_IN_WORLD_ERROR = new DynamicCommandExceptionType(o -> new ChatMessage(Commands.TPW_ALREADY_IN_WORLD, o));
	public static final DynamicCommandExceptionType NOT_SET_ERROR = new DynamicCommandExceptionType(o -> new ChatMessage("These aren't set yet: %s", Utils.joinNiceString(((List<String>) o).toArray())));
	public static final DynamicCommandExceptionType UNKNOWN_ERROR = new DynamicCommandExceptionType(o -> new ChatComponentText("Unknown time unit " + o + "."));
	public static final DynamicCommandExceptionType UNSUPPORTED_CHAR_ERROR = new DynamicCommandExceptionType(o -> new ChatComponentText("The character " + o + " is not supported."));
	public static final SimpleCommandExceptionType ALREADY_STARTED_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Already started"));
	public static final SimpleCommandExceptionType ALREADY_STOPPED_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Not running"));
	public static final SimpleCommandExceptionType BAN_FAILED_ERROR = new SimpleCommandExceptionType(new ChatMessage("commands.ban.failed"));
	public static final SimpleCommandExceptionType NO_BLOCK_TO_SET_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Hold the block in your main hand that you want to set as the material."));
	public static final SimpleCommandExceptionType NO_POSITION_CHANGE_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Nothing changed. The supplied position is the same as the saved position."));
	public static final SimpleCommandExceptionType NOT_ENABLED_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Not enabled"));
	public static final SimpleCommandExceptionType NOT_IN_CREATIVE_ERROR = new SimpleCommandExceptionType(new ChatMessage("Please use %s to use this command", EnumGamemode.CREATIVE.c()));
	public static final SimpleCommandExceptionType NOTHING_CHANGED_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Nothing changed"));
	public static final SimpleCommandExceptionType POINTING_NOT_AT_BLOCK_ERROR = new SimpleCommandExceptionType(new ChatComponentText("You're not pointing at a block"));
	public static final SimpleCommandExceptionType STRUCTURE_NOT_FOUND_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Couldn't find the structure"));
	public static final SimpleCommandExceptionType UNSUPPORTED_STRUCTURE_TYPE_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Unsupported structure type"));

	private Messages() {
	}

	public static final class PropEdit {
		public static final String HELP = "To edit a key, click a paper. To save, press shift+right click. Close the inventory to discard changes.";
		public static final String CAN_EDIT = "You can edit this file.";
		public static final String CANT_EDIT = "You can only read this file.";
		public static final String TYPE_VALUE = "Type the desired value for %s in the chat box.";
		public static final String CLICK_TO_INSERT = "Click to insert current value into chat box";
		public static final String CANT_EDIT_OUTSIDE_SERVER = "For security reasons, you cannot modify files outside this server's folder.";
		public static final String SAVE_SUCCESS = "Successfully saved the file.";
		public static final String SAVE_FAILED = "Failed saving the file.";
		public static final String NOT_SET = "<not set>";
	}

	public static final class Authenticator {
		public static final String CLEARED_PASSWORD = "Successfully cleared your password. Warning: anyone can use the password protected features.";
		public static final String NOT_YET_SET = "You have not yet set a password.";
		public static final String NOT_MATCH = "New password and confirm password doesn't match.";
		public static final String TOO_WEAK = "Your chosen password is too weak. Passwords should be mixed-case, contain both letters and numbers, and should ideally be more than 8 characters in length and contain non-alphanumeric characters. Certain common passwords are also prohibited.";
		public static final String STRENGTH = "Strength: %s";
		public static final String CHANGED_PASSWORD = "Successfully changed your password.";
	}

	public static final class ChatCaps {
		public static final String WARN_MINIMIZE_CAPS = "Minimize your usage of uppercase letters!";
	}

	public static final class BanTnt {
		public static final String PREVENTED_BLOCKS = "%d block(s) are prevented from being destroyed!";
		public static final String REMOVED_TNTS = " Also, %d primed TNT(s) are removed!";
	}

	public static final class DamageSummary {
		public static final String TITLE = "Damage summary since your last death";
	}

	public static final class Commands {
		public static final String NO_PERM = "You do not have sufficient privileges to execute this command";
		public static final String PLUGIN_DISABLED = "The plugin is disabled, so its commands are disabled too.";
		public static final String TPW_NONEXISTENT_TITLE = "The world %s doesn't exist.";
		public static final String TPW_SUCCESS = "Teleported %s to world %s";
		public static final String TPW_TELEPORTING = "Teleporting you to world %s...";
		public static final String TPW_ALREADY_IN_WORLD = "You're already in that world (%s).";
	}

	public static final class PlayerHist {
		public static final String OFF_MODE = "This server must be in online mode!";
		public static final String HEADER = "Fetching player name history of " + ChatColor.BOLD + "%s" + ChatColor.RESET + "...";
		public static final String NOT_FOUND = "That player cannot be found";
		public static final String UUID_IS = "%s's UUID is %s";
		public static final String NUMBER = ChatColor.YELLOW + "%s" + ChatColor.RESET + " %s";
		public static final String INITIAL_NAME = "initial name";
		public static final String ON = "on " + ChatColor.YELLOW + "%s";
	}
}
