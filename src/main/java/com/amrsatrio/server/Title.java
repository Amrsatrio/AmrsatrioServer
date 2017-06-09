package com.amrsatrio.server;

import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_12_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
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
		subtitle = title.getSubtitle();
		fadeInTime = title.getFadeInTime();
		fadeOutTime = title.getFadeOutTime();
		stayTime = title.getStayTime();
		ticks = title.isTicks();
	}

	public Title(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeInTime = fadeInTime;
		this.stayTime = stayTime;
		this.fadeOutTime = fadeOutTime;
	}

	public static void clearTitle(Player player) {
		// Send timings first
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.CLEAR, null);
		connection.sendPacket(packet);
	}

	public static void resetTitle(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.RESET, null);
		connection.sendPacket(packet);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
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
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeInTime * (ticks ? 1 : 20), stayTime * (ticks ? 1 : 20), fadeOutTime * (ticks ? 1 : 20));
		if (fadeInTime != -1 && fadeOutTime != -1 && stayTime != -1) {
			connection.sendPacket(packet);
		}
	}

	public void updateTitle(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		ChatComponentText serialized = new ChatComponentText(ChatColor.translateAlternateColorCodes('&', title));
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TITLE, serialized);
		connection.sendPacket(packet);
	}

	public void updateSubtitle(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		ChatComponentText serialized = new ChatComponentText(ChatColor.translateAlternateColorCodes('&', subtitle));
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, serialized);
		connection.sendPacket(packet);
	}

	public void broadcast() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			send(p);
		}
	}

	public int getFadeInTime() {
		return fadeInTime;
	}

	public void setFadeInTime(int time) {
		fadeInTime = time;
	}

	public int getFadeOutTime() {
		return fadeOutTime;
	}

	public void setFadeOutTime(int time) {
		fadeOutTime = time;
	}

	public int getStayTime() {
		return stayTime;
	}

	public void setStayTime(int time) {
		stayTime = time;
	}

	public boolean isTicks() {
		return ticks;
	}
}