package com.amrsatrio.server.mapgui;

import com.amrsatrio.server.AmrsatrioServer;
import com.google.common.collect.Lists;
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

		session.currentScreen.draw(mapcanvas, player);
	}

	public void input(PhoneInput phoneInput, Player p) {
		if (!sessions.containsKey(p)) {
			throw new IllegalStateException("The player " + p.getName() + " doesn't have a phone map session!");
		}

		Session session = sessions.get(p);
		if (phoneInput == PhoneInput.HOME) {
			session.screen(null);
		} else {
			session.currentScreen.input(phoneInput);
		}
	}

	public enum PhoneInput {
		UP, DOWN, LEFT, RIGHT, BUTTON1, BUTTON2, HOME
	}

	private static class Session {
		public PhoneScreen currentScreen;

		public void screen(PhoneScreen phonescreen) {
			currentScreen = phonescreen;
			if (phonescreen == null) {
				return;
			}
			phonescreen.session = this;
			phonescreen.width = 128;
			phonescreen.height = 128;
			phonescreen.init();
		}
	}

	private static class TestListScreen extends ListScreen {

		public TestListScreen(DrawableMapRenderer mapRenderer) {
			super(mapRenderer, "Test list", Lists.newArrayList("TEST 1", "TEST 2", "LOOW", "TEST 4", "TEST 123", "Nice job"));
		}
	}

	private static class TestScreen extends PhoneScreen {
		public TestScreen(DrawableMapRenderer maprenderer) {
			super(maprenderer);
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
			maprenderer.font(NokiaFontSmall.FONT);
			int i = maprenderer.font().getHeight() + 1;
			String s = "Test everything";
			drawTop(s);
			maprenderer.str(1, 15, maprenderer.wrapFormattedStringToWidth(SDF.format(System.currentTimeMillis()) + "\n" + AmrsatrioServer.NMS_VERSION + "\n" + player.getName() + "\n" + player.getWorld().getName(), 128), true);
			drawBottom("Select", "Back");
		}

		@Override
		protected void input(PhoneInput input) {
			if (input == PhoneInput.DOWN) {
				session.screen(new TestListScreen(maprenderer));
			}
		}
	}

	private static class ListScreen extends PhoneScreen {
		private final List<String> items;
		private final String title;
		private int cursor = 0;

		public ListScreen(DrawableMapRenderer mapRenderer, String title, List<String> strings) {
			super(mapRenderer);
			this.title = title;
			items = strings;
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
//			super.draw(mapcanvas, player, session);
			int i = drawTop(title);
			int scrollbarHeight = height - 26;
			int thumbHeight = scrollbarHeight / items.size();
			maprenderer.rect(width - 2, i, 2, scrollbarHeight, true);
			maprenderer.rect(width - 2, (int) (i + ((float) cursor / (float) items.size() * (float) scrollbarHeight)), 2, thumbHeight, false);
			int j = maprenderer.font().getHeight() + 2;

			for (int i1 = 0; i1 < items.size(); i1++) {
				if (cursor == i1) {
					maprenderer.rect(0, i, 126, j, true);
				}

				maprenderer.str(1, i + 1, items.get(i1), cursor != i1);
				i += j;
			}

			drawBottom("Select", "Back");
		}

		@Override
		protected void input(PhoneInput input) {
			switch (input) {
				case UP:
				case LEFT:
					cursor--;
					break;
				case DOWN:
				case RIGHT:
					cursor++;
					break;
			}
		}
	}

	private static class PhoneScreen {
		public int width;
		public int height;
		protected DrawableMapRenderer maprenderer;
		protected Session session;

		public PhoneScreen(DrawableMapRenderer maprenderer) {
			this.maprenderer = maprenderer;
		}

		public void init() {

		}

		public void draw(MapCanvas mapcanvas, Player player) {

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

			return maprenderer.font().getHeight() + 1;
		}

		private void ctrStrBox(int x, int y, int w, String text, boolean black) {
			if (!black) {
				maprenderer.rect(x, y, w, 13, true);
			}

			maprenderer.ctrStr(x + w / 2, y + 1, text, black);
		}

		protected void input(PhoneInput input) {

		}
	}
}
