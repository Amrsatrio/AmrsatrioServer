package com.amrsatrio.server.mapphone;

import com.amrsatrio.server.PingList.PingEntry;
import com.amrsatrio.server.RayTrace;
import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.Utils;
import com.google.common.collect.Collections2;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.amrsatrio.server.ServerPlugin.SDF;

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
		MapView mapView = worldmap.mapView;

		for (MapRenderer maprenderer : mapView.getRenderers()) {
			mapView.removeRenderer(maprenderer);
		}

		mapView.addRenderer(this);
		ServerPlugin.LOGGER.debug("Phone map ID: " + mapView.getId());
	}

	@Override
	public void draw(MapView mapview, MapCanvas mapcanvas, Player player) {
		if (!sessions.containsKey(player)) {
			sessions.put(player, new PhoneSession(player, this));
		}

		PhoneSession session = sessions.get(player);

		if (session.currentScreen == null) {
			session.displayScreen(new MainScreen());
		}

		try {
			session.currentScreen.draw(mapcanvas, player);
		} catch (Throwable e) {
			player.getInventory().remove(new ItemStack(Material.MAP));
			sessions.remove(player);
			Utils.broke(e, " while rendering map");
		}
	}

	public void sendInput(PhoneInput phoneInput, Player p) {
		if (!sessions.containsKey(p)) {
			throw new IllegalStateException("The player " + p.getName() + " doesn't have a phone map session!");
		}

		PhoneSession session = sessions.get(p);

		if (phoneInput == PhoneInput.END) {
			session.displayScreen(null);
		} else {
			session.currentScreen.onInput(phoneInput);
		}
	}

	public enum PhoneInput {
		UP("up"), DOWN("down"), LEFT("left"), RIGHT("right"), BUTTON1("button1"), BUTTON2("button2"), END("end");

		private static final Map<String, PhoneInput> BY_NAME = new HashMap<>();

		static {
			for (PhoneInput phoneinput : values()) {
				BY_NAME.put(phoneinput.name, phoneinput);
			}
		}

		private final String name;

		PhoneInput(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		public static PhoneInput getByName(String name) {
			return BY_NAME.get(name);
		}
	}

	private static class PhoneSession {
		public Screen currentScreen;
		public Player player;
		public DrawableMapRenderer mapRenderer;

		public PhoneSession(Player player, DrawableMapRenderer mapRenderer) {
			this.player = player;
			this.mapRenderer = mapRenderer;
		}

		public void displayScreen(Screen screen) {
			currentScreen = screen;

			if (screen == null) {
				return;
			}

			screen.session = this;
			screen.mapRenderer = mapRenderer;
			screen.width = 128;
			screen.height = 128;
			screen.init();
		}
	}

	private static class TestListScreen extends ListScreen {
		private static final String SEL_GM = "Change gamemode";
		private static final String CMD_BLK = "Cmd. block info";
		private static final String PING_HIST = "Ping history";
		private static final String TEST_INFO = "Test info";

		public TestListScreen(Screen screen) {
			super(screen, "Actions", Arrays.asList(SEL_GM, CMD_BLK, PING_HIST, TEST_INFO));
		}

		@Override
		protected void onSelected(int i, String s) {
			switch (s) {
				case SEL_GM:
					session.displayScreen(new SelectGamemodeScreen(this));
					break;

				case CMD_BLK:
					session.displayScreen(new CommandBlockInfo(this));
					break;

				case PING_HIST:
					session.displayScreen(new PingHistScreen(this));
					break;

				case TEST_INFO:
					session.displayScreen(new WarningScreen(this, WarningScreen.EnumInfoType.WARNING, "Good, u clicked me!"));
					break;
			}
		}
	}

	private static class SelectGamemodeScreen extends ListScreen {
		public SelectGamemodeScreen(Screen screen) {
			super(screen, "Change gamemode", Arrays.asList("Survival", "Creative", "Adventure", "Spectator"));
		}

		@Override
		protected void onSelected(int i, String s) {
			GameMode gamemode = GameMode.valueOf(s.toUpperCase());

			if (session.player.getGameMode() == gamemode) {
				session.displayScreen(new WarningScreen(this, WarningScreen.EnumInfoType.WARNING, "You're on that game mode!"));
				return;
			}

			session.player.setGameMode(gamemode);
			session.displayScreen(new WarningScreen(prevScreen, WarningScreen.EnumInfoType.INFO, "Done"));
		}
	}

	private static class PingHistScreen extends ListScreen {
		private static final Function<PingEntry, String> PENTRY_TO_STR = new Function<PingEntry, String>() {
			@Nullable
			@Override
			public String apply(@Nullable PingEntry pingEntry) {
				return pingEntry.getKey() + " (" + pingEntry.times.size() + ")";
			}
		};

		public PingHistScreen(Screen screen) {
			super(screen, "Ping history", new ArrayList<>(Collections2.transform(ServerPlugin.getInstance().pingList.getValues(), PENTRY_TO_STR::apply)));
		}


	}

//	private static class PlayerAddressesScreen extends TextScreen {
//
//	}

	private static class CommandBlockInfo extends Screen {
		private static final Function<net.minecraft.server.v1_12_R1.ItemStack, String> ITEM_STACK_STRING_FUNCTION = new Function<net.minecraft.server.v1_12_R1.ItemStack, String>() {
			@Nullable
			@Override
			public String apply(@Nullable net.minecraft.server.v1_12_R1.ItemStack itemStack) {
				return itemStack.isEmpty() ? "ERROR" : itemStack.getCount() + " * " + itemStack.getName();
			}
		};

		public CommandBlockInfo(Screen screen) {
			super(screen);
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
			drawTop("Block entity info");
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			RayTrace raytrace = RayTrace.a(player, 5.0D);
			BlockPosition blockposition = raytrace.f();
			StringBuilder s = new StringBuilder("Point at a block entity");

			if (raytrace.a() && blockposition != null) {
				TileEntity tileentity = ((CraftWorld) player.getWorld()).getHandle().getTileEntity(blockposition);

				if (tileentity instanceof TileEntityCommand) {
					s = new StringBuilder(((TileEntityCommand) tileentity).getCommandBlock().getCommand());
				} else if (tileentity instanceof TileEntityLootable) {
					s = new StringBuilder();

					for (net.minecraft.server.v1_12_R1.ItemStack itemstack : ((TileEntityLootable) tileentity).getContents().stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList())) {
						s.append(ITEM_STACK_STRING_FUNCTION.apply(itemstack)).append('\n');
					}
				}
			}

			mapRenderer.str(1, 15, mapRenderer.wrapFormattedStringToWidth(s.toString(), width - 2), true);
			drawBottom();
		}

		@Override
		protected String getLeftText() {
			return "";
		}
	}

	private static class WarningScreen extends Screen {
		private static final int HOW_LONG = 60;
		private final EnumInfoType type;
		private final String text;
		private int elapsed;

		public WarningScreen(Screen screen, EnumInfoType type, String s) {
			super(screen);
			this.type = type;
			text = s;
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
			if (elapsed == 0) {
				player.playSound(player.getLocation(), "minecraft:block.note.pling", 1.0F, type == EnumInfoType.WARNING ? 0.7F : 1.4F);
			}

			if (elapsed > HOW_LONG) {
				session.displayScreen(prevScreen);
			}

			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			mapRenderer.str(0, 0, mapRenderer.wrapFormattedStringToWidth(text, width), true);
			elapsed++;
		}

		public enum EnumInfoType {
			INFO, WARNING
		}
	}

	private static class MainScreen extends Screen {
		public MainScreen() {
			super(null);
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
			drawTop("Overview");
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			mapRenderer.str(1, 15, mapRenderer.wrapFormattedStringToWidth(SDF.format(System.currentTimeMillis()) + "\n" + ServerPlugin.NMS_VERSION + "\n" + player.getName() + "\n" + player.getWorld().getName(), width - 2), true);
			drawBottom();
		}

		@Override
		protected String getLeftText() {
			return "Actions";
		}

		@Override
		protected String getRightText() {
			return "";
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
		private int drawStart = 0;
		private int displayableItems;
		private boolean doesScroll;

		public ListScreen(Screen screen, String title, List<String> strings) {
			super(screen);
			this.title = title;
			items = strings;
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
//			super.draw(mapcanvas, player, session);
			int theY = drawTop(title);
			mapRenderer.str(getTopWidth(), 0, "" + (cursor + 1), true);
			int scrollbarHeight = height - 26;
			int scrollbarWidth = 4;
			int thumbHeight = scrollbarHeight / items.size();
			mapRenderer.rect(width - scrollbarWidth, theY, scrollbarWidth, scrollbarHeight, true);
			mapRenderer.rect(width - scrollbarWidth, (int) (theY + (float) cursor / (float) items.size() * (float) scrollbarHeight), scrollbarWidth, thumbHeight, false);
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			int itemHeight = mapRenderer.getFont().getHeight() + 2;
			displayableItems = scrollbarHeight / itemHeight;
			doesScroll = items.size() > displayableItems;
//			Utils.actionBarWithoutReflection(player, new ChatComponentText(String.format("Items %d, displayable %d, drawStart %d, cursor %d, bottom %s, top %s", items.size(), displayableItems, drawStart, cursor, isCursorAtBottomOfScreen(), isCursorAtTopOfScreen())));

			for (int i = 0; i < displayableItems; i++) {
				drawEntry(theY, width - scrollbarWidth, itemHeight, (drawStart + i) % items.size());
				theY += itemHeight;

				if (!doesScroll && i >= items.size() - 1) {
					break;
				}
			}

			drawBottom();
			// LOGIC if displayableItems == 3 items.size() == 5
			// drawStart 2 draw 2, 3, 4
			// drawStart 3 draw 3, 4, 5
			// drawStart 4 draw 4, 1, 2
			// drawStart 5 INVALID!!!
			// isCursorAtTheBottom == true ARE FOLLOWS
			// drawStart 0 cursor 2
			// drawStart 4 cursor 1
		}

		private void drawEntry(int y, int w, int h, int idx) {
			if (cursor == idx) {
				mapRenderer.rect(0, y, w, h, true);
			}

			mapRenderer.str(1, y + 1, items.get(idx), cursor != idx);
		}

		@Override
		protected void onInput(PhoneInput input) {
			super.onInput(input);
			switch (input) {
				case UP:
				case LEFT:
					if (isCursorAtTopOfScreen() && doesScroll) {
						drawStart = mod(drawStart - 1, items.size());
					}

					if (cursor > 0) {
						cursor--;
					} else {
						cursor = items.size() - 1;
					}

					break;

				case DOWN:
				case RIGHT:
					if (isCursorAtBottomOfScreen() && doesScroll) {
						drawStart = mod(drawStart + 1, items.size());
					}

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

		private static int mod(int a, int b) {
			int r = a % b;
			return r < 0 ? r + b : r;
		}

		protected void onSelected(int i, String s) {
		}

		private boolean isCursorAtTopOfScreen() {
			return drawStart == cursor;
		}

		private boolean isCursorAtBottomOfScreen() {
			return (drawStart + displayableItems - 1) % items.size() == cursor;
		}

		@Override
		protected int getTopWidth() {
			return width - mapRenderer.getFont().getWidth("" + (cursor + 1)) - 1;
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
			prevScreen = screen;
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

		protected String getLeftText() {
			return "Select";
		}

		protected String getRightText() {
			return "Back";
		}

		protected int drawBottom() {
			int center = width / 2;
			int j = height - 13;
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			ctrStrBox(0, j, center, getLeftText(), !leftButtonOn);
			ctrStrBox(center, j, center, getRightText(), !rightButtonOn);
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
			session.player.playSound(session.player.getLocation(), "minecraft:block.note.harp", 1.0F, 0.7F);

			switch (input) {
				case BUTTON2:
					session.displayScreen(prevScreen);
					rightButtonOn = true;
					break;
				case BUTTON1:
					leftButtonOn = true;
					break;
			}
		}
	}
}
