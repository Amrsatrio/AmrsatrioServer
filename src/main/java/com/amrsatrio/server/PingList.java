package com.amrsatrio.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_11_R1.JsonList;
import net.minecraft.server.v1_11_R1.JsonListEntry;
import org.apache.commons.lang3.time.DateUtils;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;

public class PingList extends JsonList<String, PingList.PingEntry> {
	public static class PingEntry extends JsonListEntry<String> {
		public List<GameProfile> names;
		public List<Long> times;

		public PingEntry(String ip, List<GameProfile> names, List<Long> times) {
			super(ip);
			this.names = names;
			this.times = times;
		}

		public PingEntry(JsonObject jsonobject) {
			super(jsonobject.get("ip").getAsString(), jsonobject);
//			System.out.println("reading " + jsonobject);
			this.names = new ArrayList<>();
			this.times = new ArrayList<>();

			for (JsonElement jsonelement : jsonobject.get("names").getAsJsonArray()) {
				names.add(constructProfile(jsonelement.getAsJsonObject()));
			}

			for (JsonElement jsonelement : jsonobject.get("times").getAsJsonArray()) {
				try {
					times.add(AmrsatrioServer.SDF.parse(jsonelement.getAsString()).getTime());
				} catch (ParseException e) {
					AmrsatrioServer.LOGGER.warn("Failed to parse date element {}, skipping. Is it edited?", jsonelement);
				}
			}
		}

		@Override
		protected void a(JsonObject jsonObject) {
			jsonObject.addProperty("ip", getKey());
			super.a(jsonObject);
			JsonArray jsonarray = new JsonArray();

			for (GameProfile gameprofile : names) {
				JsonObject jsonObject1 = new JsonObject();
				jsonObject1.addProperty("uuid", gameprofile.getId() == null ? "" : gameprofile.getId().toString());
				jsonObject1.addProperty("name", gameprofile.getName());
				jsonarray.add(jsonObject1);
			}

			jsonObject.add("names", jsonarray);
			jsonObject.add("times", constructJsonTimeArray(times));
		}

		public int getHowManyTimesPingedToday() {
			return Collections2.filter(this.times, new Predicate<Long>() {
				@Override
				public boolean apply(@Nullable Long aLong) {
					Date date = new Date(aLong);
					return aLong >= DateUtils.truncate(date, Calendar.DATE).getTime() && aLong < DateUtils.addMilliseconds(DateUtils.ceiling(date, Calendar.DATE), -1).getTime();
				}
			}).size();
		}
	}

	public PingList(File file) {
		super(file);
	}

	@Override
	protected PingEntry a(JsonObject jsonobject) {
		return new PingEntry(jsonobject);
	}

	public PingEntry get(InetAddress var1) {
		String var2 = this.c(var1);
		return this.get(var2);
	}

	private String c(InetAddress var1) {
		String var2 = var1.toString();
		if (var2.contains("/")) {
			var2 = var2.substring(var2.indexOf(47) + 1);
		}

		if (var2.contains(":")) {
			var2 = var2.substring(0, var2.indexOf(58));
		}

		return var2;
	}

	private static JsonArray constructJsonTimeArray(List<Long> list) {
		JsonArray jsonarray = new JsonArray();

		for (long l : list) {
			jsonarray.add(new JsonPrimitive(AmrsatrioServer.SDF.format(l)));
		}

		return jsonarray;
	}

	private static GameProfile constructProfile(JsonObject jsonobject) {
		if (jsonobject.has("uuid") && jsonobject.has("name")) {
			String s = jsonobject.get("uuid").getAsString();
			UUID uuid;

			try {
				uuid = UUID.fromString(s);
			} catch (Throwable var4) {
				return null;
			}

			return new GameProfile(uuid, jsonobject.get("name").getAsString());
		} else {
			return null;
		}
	}

	@Override
	public void save() throws IOException {
	}

	private void save2() {
		try {
			super.save();
		} catch (IOException var3) {
			a.warn("Could not save ping list.", var3);
		}
	}

	public void addEntry(ServerListPingEvent event) {
		String s = c(event.getAddress());
		PingEntry pingentry = getOrCreate(s);
		pingentry.times.add(System.currentTimeMillis());
		save2();
	}

	public void addEntry(Player player) {
		String s = c(player.getAddress().getAddress());
		PingEntry pingentry = getOrCreate(s);
		GameProfile gameprofile = ((CraftPlayer) player).getProfile();

		if (!pingentry.names.contains(gameprofile)) {
			pingentry.names.add(gameprofile);
		}

		save2();
	}

	private PingEntry getOrCreate(String s) {
		if (!d(s)) {
			add(new PingEntry(s, Lists.<GameProfile>newArrayList(), Lists.<Long>newArrayList()));
		}

		return get(s);
	}
}
