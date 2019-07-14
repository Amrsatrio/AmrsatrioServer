package com.amrsatrio.server.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.CommandListenerWrapper;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConfigManager {
	private static final SimpleCommandExceptionType NO_SUCH_CONFIG_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Invalid configuration key"));
	private static final SimpleCommandExceptionType NOT_CHANGED_ERROR = new SimpleCommandExceptionType(new ChatComponentText("Not changed"));
	private final Plugin plugin;
	private Set<PluginConfig> configList = new HashSet<>();
	//    private Map<String, PluginConfig> fKeyToCfgMap = new HashMap<>();
	private Map<String, PluginConfig> cKeyToCfgMap = new HashMap<>();

	public ConfigManager(Plugin plugin) {
		this.plugin = plugin;
		registerConfig("disable-tnt", "remove_tnts", "Prevent TNT interacting with blocks", false);
		registerConfig("broadcast-exceptions", "broadcast_errors", "Broadcast plugin errors", false);
		registerConfig("enable-map-gui", "enable_map_gui", "Map GUI state (requires plugin reload)", false);
		registerConfig("enable-server-ping-records", "enable_server_ping_records", "Record server status queries (requires plugin reload)", false);
		registerConfig("show-damage-summary", "show_damage_summary", "Enable on death damage summary for all players", false);
	}

	private void registerConfig(String inFileKey, String inCommandKey, String description, boolean defaultValue) {
		PluginConfig pluginConfig = new PluginConfig(plugin, inFileKey, inCommandKey, description, defaultValue);
		configList.add(pluginConfig);
		cKeyToCfgMap.put(inCommandKey, pluginConfig);
	}

	public int executeInCommand(CommandListenerWrapper listener, String providedKey, boolean set, boolean setValue) throws CommandSyntaxException {
		PluginConfig config = getConfig(providedKey);

		if (config == null) {
			throw NO_SUCH_CONFIG_ERROR.create();
		}

		if (set) {
			if (config.get() == setValue) {
				throw NOT_CHANGED_ERROR.create();
			}

			config.set(setValue);
			listener.sendMessage(config.componentRepresentation(), true);
		} else {
			listener.sendMessage(config.componentRepresentation(), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	public PluginConfig getConfig(String key) {
		return cKeyToCfgMap.get(key);
	}

	public Set<String> getKeys() {
		return cKeyToCfgMap.keySet();
	}

	public static class PluginConfig {
		private Plugin plugin;
		public String fileKey, commandKey, description;
		public boolean defaultValue, setValue;

		private PluginConfig(Plugin plugin, String fileKey, String commandKey, String description, boolean defaultValue) {
			this.plugin = plugin;
			this.fileKey = fileKey;
			this.commandKey = commandKey;
			this.description = description;
			this.defaultValue = defaultValue;
			setValue = plugin.getConfig().getBoolean(fileKey, defaultValue);
		}

		public boolean get() {
			return setValue;
		}

		public void set(boolean value) {
			setValue = value;
			plugin.getConfig().set(fileKey, value);
			plugin.saveConfig();
		}

		public IChatBaseComponent componentRepresentation() {
			return new ChatMessage("%s: %s", new ChatComponentText(description), Utils.disabledOrEnabledComponent(get()));
		}
	}
}
