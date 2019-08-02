package com.amrsatrio.server.mapphone;

import com.amrsatrio.server.ServerPlugin;
import com.amrsatrio.server.util.PingList;
import com.amrsatrio.server.util.PingList.PingEntry;
import com.amrsatrio.server.util.RayTracer;
import com.amrsatrio.server.util.Utils;
import com.google.common.collect.Collections2;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.IInventory;
import net.minecraft.server.v1_14_R1.MinecraftKey;
import net.minecraft.server.v1_14_R1.MovingObjectPositionBlock;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.TileEntity;
import net.minecraft.server.v1_14_R1.TileEntityCommand;
import net.minecraft.server.v1_14_R1.TileEntityLootable;
import net.minecraft.server.v1_14_R1.TileEntityTypes;
import net.minecraft.server.v1_14_R1.WorldMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.amrsatrio.server.ServerPlugin.SDF;

public class PhoneMap extends DrawableMapRenderer {
	private static Map<Player, PhoneSession> sessions = new HashMap<>();

	public void init() {
		WorldMap worldmap = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle().a("map_" + Short.MAX_VALUE);

		if (worldmap == null) {
			ServerPlugin.LOGGER.warn("Initialization of fake map failed (null returned)");
			return;
		}

		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setByte("scale", (byte) 4);
		nbttagcompound.setByte("dimension", (byte) 0);
		nbttagcompound.setShort("height", (short) 128);
		nbttagcompound.setShort("width", (short) 128);
		nbttagcompound.setInt("xCenter", Integer.MAX_VALUE);
		nbttagcompound.setInt("yCenter", Integer.MAX_VALUE);
		nbttagcompound.setByteArray("colors", new byte[0]);
		worldmap.a(nbttagcompound);
		MapView mapView = worldmap.mapView;

		for (MapRenderer maprenderer : mapView.getRenderers()) {
			mapView.removeRenderer(maprenderer);
		}

		mapView.addRenderer(this);
		ServerPlugin.LOGGER.debug("Phone map ID: " + mapView.getId());
	}

	@Override
	public void draw(MapView mapview, MapCanvas mapcanvas, Player player) {
		PhoneSession session;

		if ((session = sessions.get(player)) == null) {
			sessions.put(player, session = new PhoneSession(player, this));
		}

		if (session.currentScreen == null) {
			session.displayScreen(new MainScreen());
		}

		try {
			session.currentScreen.draw(mapcanvas, player);
		} catch (Throwable e) {
			player.getInventory().remove(new ItemStack(Material.MAP));
			session.stopDrawing = true;
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
		public MapGuiScreen currentScreen;
		public Player player;
		public DrawableMapRenderer mapRenderer;
		public boolean stopDrawing;

		public PhoneSession(Player player, DrawableMapRenderer mapRenderer) {
			this.player = player;
			this.mapRenderer = mapRenderer;
		}

		public void displayScreen(MapGuiScreen screen) {
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
		private static final String CMD_BLK = "Block entity info";
		private static final String PING_HIST = "Ping history";
		private static final String TEST_INFO = "Test info";

		public TestListScreen(MapGuiScreen screen) {
			super(screen, "Actions", Arrays.asList(SEL_GM, CMD_BLK, PING_HIST, TEST_INFO));
		}

		@Override
		protected void onSelected(int i, String s) {
			switch (s) {
				case SEL_GM:
					session.displayScreen(new SelectGamemodeScreen(this));
					break;

				case CMD_BLK:
					session.displayScreen(new CommandBlockInfoScreen(this));
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
		public SelectGamemodeScreen(MapGuiScreen screen) {
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
		private static final Function<PingEntry, String> PENTRY_TO_STR = pingEntry -> pingEntry.getKey() + " (" + pingEntry.times.size() + ")";

		public PingHistScreen(MapGuiScreen screen) {
			super(screen, "Ping history", null);
			PingList pingList = ServerPlugin.getInstance().pingList;
			setItems(pingList == null ? Collections.singletonList("Ping list is disabled") : new ArrayList<>(Collections2.transform(pingList.getValues(), PENTRY_TO_STR::apply)));
		}
	}

//	private static class PlayerAddressesScreen extends TextScreen {
//
//	}

	private static class CommandBlockInfoScreen extends MapGuiScreen {
		private static final Function<net.minecraft.server.v1_14_R1.ItemStack, String> ITEM_STACK_STRING_FUNCTION = itemStack -> itemStack.isEmpty() ? "ERROR" : itemStack.getCount() + " * " + itemStack.getName();

		public CommandBlockInfoScreen(MapGuiScreen screen) {
			super(screen);
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
			drawTop("Block entity info");
			mapRenderer.setFont(NokiaFont.SMALL);
			MovingObjectPositionBlock hitResult = RayTracer.rayTrace(player, 5.0D);
			BlockPosition blockposition = hitResult.getBlockPosition();
			StringBuilder s = new StringBuilder("Point at a block entity");

			if (hitResult.d() && blockposition != null) {
				TileEntity tileentity = ((CraftWorld) player.getWorld()).getHandle().getTileEntity(blockposition);

				if (tileentity instanceof TileEntityCommand) {
					s = new StringBuilder(((TileEntityCommand) tileentity).getCommandBlock().getCommand());
				} else if (tileentity instanceof IInventory) {
					s = new StringBuilder();

					labelLootable:
					{
						MinecraftKey ltKey;

						if (tileentity instanceof TileEntityLootable && (ltKey = ((TileEntityLootable) tileentity).lootTable) != null) {
							String ltSeed;

							try {
								Field n = TileEntityLootable.class.getDeclaredField("lootTableSeed");
								n.setAccessible(true);
								long theSeed = (long) n.get(tileentity);

								if (theSeed == 0L) {
									ltSeed = "random seed";
								} else {
									ltSeed = "seed \"" + theSeed + "\"";
								}
							} catch (ReflectiveOperationException e) {
								ltSeed = "unknown seed (" + e + ")";
							}

							s.append("Using loot table \"" + ltKey + "\" with " + ltSeed + ". Contents not yet generated");
							break labelLootable;
						}

						if (!((IInventory) tileentity).isNotEmpty()) {
							s.append("Container is empty");
							break labelLootable;
						}

						for (net.minecraft.server.v1_14_R1.ItemStack itemstack : ((IInventory) tileentity).getContents().stream().filter(itemStack -> !itemStack.isEmpty()).collect(Collectors.toList())) {
							s.append(ITEM_STACK_STRING_FUNCTION.apply(itemstack)).append('\n');
						}
					}
				} else if (tileentity != null) {
					s = new StringBuilder("Block entity " + TileEntityTypes.a(tileentity.q()) + " is not supported yet");
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

	private static class WarningScreen extends MapGuiScreen {
		private static final int HOW_LONG = 60;
		private final EnumInfoType type;
		private final String text;
		private int elapsed;

		public WarningScreen(MapGuiScreen screen, EnumInfoType type, String s) {
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

	private static class MainScreen extends MapGuiScreen {
		public MainScreen() {
			super(null);
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
			drawTop("Overview");
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			mapRenderer.str(1, 15, mapRenderer.wrapFormattedStringToWidth(SDF.format(System.currentTimeMillis()) + "\n" + Bukkit.getBukkitVersion() + "\n" + player.getName() + "\n" + player.getWorld().getName() + "\n" + ServerPlugin.getInstance().cmdBlockList.size(), width - 2), true);
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

	private static class ListScreen extends MapGuiScreen {
		private List<String> items;
		private final String title;
		private int cursor = 0;
		private int drawStart = 0;
		private int displayableItems;
		private boolean doesScroll;

		public ListScreen(MapGuiScreen lastScreen, String title, List<String> items) {
			super(lastScreen);
			this.title = title;
			this.items = items;
		}

		@Override
		public void draw(MapCanvas mapcanvas, Player player) {
//			super.draw(mapcanvas, player, session);
			int theY = drawTop(title);
			mapRenderer.str(getTopWidth(), 0, "" + (cursor + 1), true);
			int scrollbarHeight = height - 26;
			int scrollbarWidth = 4;
			int thumbHeight = scrollbarHeight / items.size();
			mapRenderer.fill(width - scrollbarWidth, theY, scrollbarWidth, scrollbarHeight, true);
			mapRenderer.fill(width - scrollbarWidth, (int) (theY + (float) cursor / (float) items.size() * (float) scrollbarHeight), scrollbarWidth, thumbHeight, false);
			mapRenderer.setFont(NokiaFont.BOLD_SMALL);
			int itemHeight = mapRenderer.getFont().getHeight() + 2;
			displayableItems = scrollbarHeight / itemHeight;
			doesScroll = items.size() > displayableItems;
//			Utils.actionBar(player, new ChatComponentText(String.format("Items %d, displayable %d, drawStart %d, cursor %d, bottom %s, top %s", items.size(), displayableItems, drawStart, cursor, isCursorAtBottomOfScreen(), isCursorAtTopOfScreen())));

			for (int i = 0; i < displayableItems; i++) {
				drawEntry(theY, width - scrollbarWidth, itemHeight, (drawStart + i) % items.size());
				theY += itemHeight;

				if (!doesScroll && i >= items.size() - 1) {
					break;
				}
			}

			drawBottom();
		}

		private void drawEntry(int y, int w, int h, int idx) {
			if (cursor == idx) {
				mapRenderer.fill(0, y, w, h, true);
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

		public void setItems(List<String> items) {
			this.items = items;
		}

		private static int mod(int a, int b) {
			int r = a % b;
			return r < 0 ? r + b : r;
		}
	}

	private static class MapGuiScreen {
		public int width;
		public int height;
		public MapGuiScreen prevScreen;
		protected DrawableMapRenderer mapRenderer;
		protected PhoneSession session;
		private boolean leftButtonOn;
		private boolean rightButtonOn;

		public MapGuiScreen(MapGuiScreen screen) {
			prevScreen = screen;
		}

		public void init() {

		}

		public void draw(MapCanvas mapcanvas, Player player) {

		}

		protected int drawTop(String s) {
			mapRenderer.setFont(NokiaFont.SMALL);
			int i = mapRenderer.getFont().getHeight() + 1;
//			mapRenderer.fill(0, 0, getTopWidth(), i, true);
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
				mapRenderer.fill(x, y, w, 13, true);
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
