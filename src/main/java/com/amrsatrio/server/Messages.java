package com.amrsatrio.server;

import org.bukkit.ChatColor;

public enum Messages {
	;

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
		public static final String PERM_SYSINFO = "Allow user to view system information";
		public static final String TPW_NONEXISTENT_TITLE = "The world %s doesn't exist. Existing worlds are:";
		public static final String TPW_SUCCESS = "Teleported %s to world %s";
		public static final String TPW_TELEPORTING = "Teleporting you to world %s...";
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
