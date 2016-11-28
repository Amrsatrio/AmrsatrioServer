package com.amrsatrio.server.mapphone;

import com.amrsatrio.server.AmrsatrioServer;
import com.google.common.collect.Lists;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amrsatrio.server.AmrsatrioServer.SDF;

public class PhoneMap extends DrawableMapRenderer {
	private static Map<Player, PhoneSession> sessions = new HashMap<>();

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
			sessions.put(player, new PhoneSession(player, this));
		}

		PhoneSession session = sessions.get(player);

		if (session.currentScreen == null) {
			session.displayScreen(new TestScreen());
		}

		session.currentScreen.draw(mapcanvas, player);
	}

	public void sendInput(PhoneInput phoneInput, Player p) {
		if (!sessions.containsKey(p)) {
			throw new IllegalStateException("The player " + p.getName() + " doesn't have a phone map session!");
		}

		PhoneSession session = sessions.get(p);

		if (phoneInput == PhoneInput.HOME) {
			session.displayScreen(null);
		} else {
			session.currentScreen.onInput(phoneInput);
		}
	}

	public enum PhoneInput {
		UP, DOWN, LEFT, RIGHT, BUTTON1, BUTTON2, HOME
	}

	private static class PhoneSession {
		public Screen currentScreen;
		public Player player;
		public DrawableMapRenderer mapRenderer;

		public PhoneSession(Player player, DrawableMapRenderer mapRenderer) {
			this.player = player;
			this.mapRenderer = mapRenderer;
		}

		public void displayScreen(Screen phonescreen) {
			currentScreen = phonescreen;

			if (phonescreen == null) {
				return;
			}

			phonescreen.session = this;
			phonescreen.mapRenderer = mapRenderer;
			phonescreen.width = 128;
			phonescreen.height = 128;
			phonescreen.init();
		}
	}

	private static class TestListScreen extends ListScreen {
		private static final String SEL_GM = "Change gamemode";

		public TestListScreen(Screen screen) {
			super(screen, "Test list", Lists.newArrayList(SEL_GM, "TEST 1", "TEST 2", "Test 3", "TEST 4", "Nice job, m8!"));
		}

		@Override
		protected void onSelected(int i, String s) {
			switch (s) {
				case SEL_GM:
					session.displayScreen(new SelectGamemodeScreen(this));
					break;
			}
		}
	}

	private static class SelectGamemodeScreen extends ListScreen {
		public SelectGamemodeScreen(Screen screen) {
			super(screen, "Change gamemode", Lists.newArrayList("Survival", "Creative", "Adventure", "Spectator"));
		}

		@Override
		protected void onSelected(int i, String s) {
			session.player.setGameMode(GameMode.valueOf(s.toUpperCase()));
			session.displayScreen(prevScreen);
		}
	}

	private static class TestScreen extends Screen {
		public TestScreen() {
			super(null);
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
			drawTop("Overview");
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			mapRenderer.str(1, 15, mapRenderer.wrapFormattedStringToWidth(SDF.format(System.currentTimeMillis()) + "\n" + AmrsatrioServer.NMS_VERSION + "\n" + player.getName() + "\n" + player.getWorld().getName(), 128), true);
			drawBottom("Actions", "");
		}

		@Override
		protected void onInput(PhoneInput input) {
			super.onInput(input);

			switch (input) {
				case BUTTON1:
					session.displayScreen(new TestListScreen(this));
					break;
			}
		}
	}

	private static class ListScreen extends Screen {
		private final List<String> items;
		private final String title;
		private int cursor = 0;

		public ListScreen(Screen screen, String title, List<String> strings) {
			super(screen);
			this.title = title;
			items = strings;
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
//			super.draw(mapcanvas, player, session);
			int i = drawTop(title);
			mapRenderer.str(getTopWidth(), 0, "" + (cursor + 1), true);
			int scrollbarHeight = height - 26;
			int scrollbarWidth = 4;
			int thumbHeight = scrollbarHeight / items.size();
			mapRenderer.rect(width - scrollbarWidth, i, scrollbarWidth, scrollbarHeight, true);
			mapRenderer.rect(width - scrollbarWidth, (int) (i + ((float) cursor / (float) items.size() * (float) scrollbarHeight)), scrollbarWidth, thumbHeight, false);
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			int j = mapRenderer.getFont().getHeight() + 2;

			for (int i1 = 0; i1 < items.size(); i1++) {
				if (cursor == i1) {
					mapRenderer.rect(0, i, 128 - scrollbarWidth, j, true);
				}

				mapRenderer.str(1, i + 1, items.get(i1), cursor != i1);
				i += j;
			}

			drawBottom("Select", "Back");
		}

		@Override
		protected void onInput(PhoneInput input) {
			super.onInput(input);
			switch (input) {
				case UP:
				case LEFT:
					if (cursor > 0) {
						cursor--;
					} else {
						cursor = items.size() - 1;
					}
					break;

				case DOWN:
				case RIGHT:
					if (cursor < items.size() - 1) {
						cursor++;
					} else {
						cursor = 0;
					}
					break;

				case BUTTON1:
					onSelected(cursor, items.get(cursor));
					break;
			}
		}

		protected void onSelected(int i, String s) {
		}

		@Override
		protected int getTopWidth() {
			return width - 6;
		}
	}

	private static class Screen {
		public int width;
		public int height;
		public Screen prevScreen;
		protected DrawableMapRenderer mapRenderer;
		protected PhoneSession session;
		private boolean leftButtonOn;
		private boolean rightButtonOn;

		public Screen(Screen screen) {
			this.prevScreen = screen;
		}

		public void init() {

		}

		public void draw(MapCanvas mapcanvas, Player player) {

		}

		protected int drawTop(String s) {
			mapRenderer.setFont(NokiaFont.SMALL);
			int i = mapRenderer.getFont().getHeight() + 1;
//			mapRenderer.rect(0, 0, getTopWidth(), i, true);
			mapRenderer.str((getTopWidth() - mapRenderer.getFont().getWidth(s)) / 2, 0, s, true);

			return i;
		}

		protected int getTopWidth() {
			return width;
		}

		protected int drawBottom(String s, String s1) {
			int i = 128;
			int center = i / 2;
			int j = i - 13;
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			ctrStrBox(0, j, center, s, !leftButtonOn);
			ctrStrBox(center, j, center, s1, !rightButtonOn);
			leftButtonOn = false;
			rightButtonOn = false;

			return mapRenderer.getFont().getHeight() + 1;
		}

		private void ctrStrBox(int x, int y, int w, String text, boolean black) {
			if (!black) {
				mapRenderer.rect(x, y, w, 13, true);
			}

			mapRenderer.ctrStr(x + w / 2, y + 1, text, black);
		}

		protected void onInput(PhoneInput input) {
			session.player.playSound(session.player.getLocation(), "minecraft:block.note.hat", 3F, 2.0F);

			switch (input) {
				case BUTTON2:
					session.displayScreen(prevScreen);
					rightButtonOn = true;
					break;
				case BUTTON1:
					session.displayScreen(prevScreen);
					leftButtonOn = true;
					break;
			}
		}
	}
}
