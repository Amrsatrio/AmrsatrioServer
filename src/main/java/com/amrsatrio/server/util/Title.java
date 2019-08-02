package com.amrsatrio.server.util;

import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_14_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_14_R1.PlayerConnection;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Title {
	private IChatBaseComponent title;
	private IChatBaseComponent subtitle;
	private int fadeInTime = -1;
	private int stayTime = -1;
	private int fadeOutTime = -1;
	private boolean ticks = false;

	public Title() {
	}

	public Title(IChatBaseComponent title) {
		this.title = title;
	}

	public Title(IChatBaseComponent title, IChatBaseComponent subtitle) {
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

	public Title(IChatBaseComponent title, IChatBaseComponent subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
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

	public IChatBaseComponent getTitle() {
		return title;
	}

	public void setTitle(IChatBaseComponent title) {
		this.title = title;
	}

	public IChatBaseComponent getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(IChatBaseComponent subtitle) {
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

		if (title != null) {
			updateTitle(player);
		}

		if (subtitle != null) {
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
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TITLE, title);
		connection.sendPacket(packet);
	}

	public void updateSubtitle(Player player) {
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, subtitle);
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