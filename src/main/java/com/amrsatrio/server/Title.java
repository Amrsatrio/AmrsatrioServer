package com.amrsatrio.server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_11_R1.ChatComponentText;
import net.minecraft.server.v1_11_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_11_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Title {
	private String title = "";
	private String subtitle = "";
	private int fadeInTime = -1;
	private int stayTime = -1;
	private int fadeOutTime = -1;
	private boolean ticks = false;

	public Title() {
	}

	public Title(String title) {
		this.title = title;
	}

	public Title(String title, String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
	}

	public Title(Title title) {
		// Copy title
		this.title = title.getTitle();
		this.subtitle = title.getSubtitle();
		this.fadeInTime = title.getFadeInTime();
		this.fadeOutTime = title.getFadeOutTime();
		this.stayTime = title.getStayTime();
		this.ticks = title.isTicks();
	}

	public Title(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeInTime = fadeInTime;
		this.stayTime = stayTime;
		this.fadeOutTime = fadeOutTime;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getSubtitle() {
		return this.subtitle;
	}

	public void setFadeInTime(int time) {
		this.fadeInTime = time;
	}

	public void setFadeOutTime(int time) {
		this.fadeOutTime = time;
	}

	public void setStayTime(int time) {
		this.stayTime = time;
	}

	public void setTimingsToTicks() {
		ticks = true;
	}

	public void setTimingsToSeconds() {
		ticks = false;
	}

	public void send(Player player) {
		resetTitle(player);
		updateTimes(player);
		updateTitle(player);

		if (!"".equals(subtitle)) {
			updateSubtitle(player);
		}
	}

	public void updateTimes(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, this.fadeInTime * (this.ticks ? 1 : 20), this.stayTime * (this.ticks ? 1 : 20), this.fadeOutTime * (this.ticks ? 1 : 20));
		if ((this.fadeInTime != -1) && (this.fadeOutTime != -1) && (this.stayTime != -1)) {
			connection.sendPacket(packet);
		}
	}

	public void updateTitle(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		ChatComponentText serialized = new ChatComponentText(ChatColor.translateAlternateColorCodes('&', this.title));
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, serialized);
		connection.sendPacket(packet);
	}

	public void updateSubtitle(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		ChatComponentText serialized = new ChatComponentText(ChatColor.translateAlternateColorCodes('&', this.subtitle));
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, serialized);
		connection.sendPacket(packet);
	}

	public void broadcast() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			send(p);
		}
	}

	public static void clearTitle(Player player) {
		// Send timings first
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, null);
		connection.sendPacket(packet);
	}

	public static void resetTitle(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.RESET, null);
		connection.sendPacket(packet);
	}

	public int getFadeInTime() {
		return fadeInTime;
	}

	public int getFadeOutTime() {
		return fadeOutTime;
	}

	public int getStayTime() {
		return stayTime;
	}

	public boolean isTicks() {
		return ticks;
	}
}