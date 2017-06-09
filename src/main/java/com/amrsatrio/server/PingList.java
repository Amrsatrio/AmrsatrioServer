package com.amrsatrio.server;

import com.amrsatrio.server.PingList.PingEntry;
import com.google.common.collect.Collections2;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.JsonList;
import net.minecraft.server.v1_12_R1.JsonListEntry;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;
import java.util.function.Predicate;

public class PingList extends JsonList<String, PingEntry> {
    public static class PingEntry extends JsonListEntry<String> {
        private static final Predicate<Long> TODAY_PREDICATE = aLong -> aLong >= Utils.getStartOfDay(System.currentTimeMillis()) && aLong <= Utils.getEndOfDay(System.currentTimeMillis());
        public Set<CustomGameProfile> names;
        public List<Long> times;

        public PingEntry(String ip, Set<CustomGameProfile> names, List<Long> times) {
            super(ip);
            this.names = names;
            this.times = times;
        }

        public PingEntry(JsonObject jsonobject) {
            super(jsonobject.get("ip").getAsString(), jsonobject);
            names = new TreeSet<>();
            times = new ArrayList<>();

            for (JsonElement jsonelement : jsonobject.get("names").getAsJsonArray()) {
                names.add(constructProfile(jsonelement.getAsJsonObject()));
            }

            for (JsonElement jsonelement : jsonobject.get("times").getAsJsonArray()) {
                try {
                    times.add(ServerPlugin.SDF.parse(jsonelement.getAsString()).getTime());
                } catch (ParseException e) {
                    ServerPlugin.LOGGER.warn("Failed to parse date element {}, skipping. Is it edited?", jsonelement);
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
            return Collections2.filter(times, TODAY_PREDICATE::test).size();
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
        String var2 = c(var1);
        return get(var2);
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
            jsonarray.add(new JsonPrimitive(ServerPlugin.SDF.format(l)));
        }

        return jsonarray;
    }

    private static CustomGameProfile constructProfile(JsonObject jsonobject) {
        if (jsonobject.has("uuid") && jsonobject.has("name")) {
            String s = jsonobject.get("uuid").getAsString();
            UUID uuid;

            try {
                uuid = UUID.fromString(s);
            } catch (Throwable var4) {
                return null;
            }

            return new CustomGameProfile(uuid, jsonobject.get("name").getAsString());
        } else {
            return null;
        }
    }

    @Override
    public void load() throws FileNotFoundException {
        super.load();
        save2();
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
        CustomGameProfile customgameprofile = new CustomGameProfile(((CraftPlayer) player).getProfile());
        pingentry.names.add(customgameprofile);
        save2();
    }

    private PingEntry getOrCreate(String s) {
        if (!d(s)) {
            add(new PingEntry(s, new TreeSet<>(), new ArrayList<>()));
        }

        return get(s);
    }

    public List<String> getIPAddressesOfPlayer(CustomGameProfile customgameprofile) {
        List<String> list = new ArrayList<>();

        for (PingEntry pingentry : getValues()) {
            if (pingentry.names.contains(customgameprofile)) {
                list.add(pingentry.getKey());
            }
        }

        return list;
    }

    public static class CustomGameProfile extends GameProfile implements Comparable<GameProfile> {
        public CustomGameProfile(UUID uuid, String s) {
            super(uuid, s);
        }

        public CustomGameProfile(GameProfile gameprofile) {
            this(gameprofile.getId(), gameprofile.getName());
        }

        @Override
        public boolean equals(Object o) {
            return getId().equals(((GameProfile) o).getId());
        }

        public int hashCode() {
            return 31 * (getId() != null ? getId().hashCode() : 0);
        }

        @Override
        public int compareTo(GameProfile o) {
            return getId().compareTo(o.getId());
        }
    }
}
