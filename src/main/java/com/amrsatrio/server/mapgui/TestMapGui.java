package com.amrsatrio.server.mapgui;

import com.amrsatrio.server.AmrsatrioServer;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amrsatrio.server.AmrsatrioServer.SDF;

public class TestMapGui extends DrawableMapRenderer {
	private static Map<Player, Session> sessions = new HashMap<>();

	public void init() {
		WorldMap worldmap = new WorldMap("map_" + Short.MAX_VALUE);
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setByte("scale", (byte) 4);
		nbttagcompound.setByte("dimension", (byte) 0);
		nbttagcompound.setShort("height", (short) 128);
		nbttagcompound.setShort("width", (short) 128);
		nbttagcompound.setInt("xCenter", Integer.MAX_VALUE);
		nbttagcompound.setInt("yCenter", Integer.MAX_VALUE);
		nbttagcompound.setByteArray("colors", new byte[0]);
		worldmap.a(nbttagcompound);
		((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().a("map_" + Short.MAX_VALUE, worldmap);
		MapView mapView = Bukkit.getMap(Short.MAX_VALUE);

		for (MapRenderer maprenderer : mapView.getRenderers()) {
			mapView.removeRenderer(maprenderer);
		}

		mapView.addRenderer(this);
		System.out.println(mapView.getId());
	}

	@Override
	public void draw(MapView mapview, MapCanvas mapcanvas, Player player) {
		if (!sessions.containsKey(player)) {
			sessions.put(player, new Session());
		}

		Session session = sessions.get(player);

		if (session.currentScreen == null) {
			session.screen(new TestScreen(this));
		}

		session.currentScreen.draw(mapcanvas, player, session);
	}

	private static class Session {
		public PhoneScreen currentScreen;

		public void screen(PhoneScreen phonescreen) {
			currentScreen = phonescreen;
			phonescreen.width = 128;
			phonescreen.height = 128;
			phonescreen.init();
		}
	}

	private static class TestScreen extends PhoneScreen {
		public TestScreen(DrawableMapRenderer maprenderer) {
			super(maprenderer);
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player, Session session) {
			maprenderer.font(NokiaFontSmall.FONT);
			int i = maprenderer.font().getHeight() + 1;
			String s = "Test everything";
			drawTop(s);
			maprenderer.str(1, 15, maprenderer.wrapFormattedStringToWidth(SDF.format(System.currentTimeMillis()) + "\n" + AmrsatrioServer.NMS_VERSION + "\n" + player.getName() + "\n" + player.getWorld().getName(), 128), true);
			drawBottom("Select", "Back");
		}
	}

	private static class ListScreen extends PhoneScreen {
		public ListScreen(DrawableMapRenderer mapRenderer, List<String> strings) {
			super(mapRenderer);
			
		}
	}

	private static class PhoneScreen {
		public int width;
		public int height;
		protected DrawableMapRenderer maprenderer;

		public PhoneScreen(DrawableMapRenderer maprenderer) {
			this.maprenderer = maprenderer;
		}

		public void init() {

		}

		public void draw(MapCanvas mapcanvas, Player player, Session session) {

		}

		protected int drawTop(String s) {
			int i = maprenderer.font().getHeight() + 1;
			maprenderer.rect(0, 0, 128, i, true);
			maprenderer.str((128 - maprenderer.font().getWidth(s)) / 2, 0, s, false);

			return i;
		}

		protected int drawBottom(String s, String s1) {
			int i = 128;
			int center = i / 2;
			int j = i - 13;
			ctrStrBox(0, j, center, s, true);
			ctrStrBox(center, j, center, s1, true);

			return 13;
		}

		private void ctrStrBox(int x, int y, int w, String text, boolean black) {
			if (!black) {
				maprenderer.rect(x, y, w, 13, true);
			}

			maprenderer.ctrStr(x + w / 2, y + 1, text, black);
		}
	}
}
