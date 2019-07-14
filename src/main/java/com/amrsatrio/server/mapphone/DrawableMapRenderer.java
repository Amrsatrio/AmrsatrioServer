package com.amrsatrio.server.mapphone;

import com.amrsatrio.server.mapphone.MapFont.CharacterSprite;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public abstract class DrawableMapRenderer extends MapRenderer {
	//private BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
	private MapView mapView;
	private MapCanvas mapCanvas;
	private MapFont font;
	private boolean clip;
	private int clipL;
	private int clipT;
	private int clipR;
	private int clipB;

	@Override
	public void render(MapView mapview, MapCanvas mapcanvas, Player player) {
		mapView = mapview;
		mapCanvas = mapcanvas;
		fill(0, 0, 128, 128, false);
		draw(mapView, mapCanvas, player);
		//Graphics graphics = image.getGraphics();
		//graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
		//graphics.drawRect(0);
		//draw(mapcanvas);
		//graphics.setColor(Color.BLUE);
		//graphics.drawRect(0, 0, image.getWidth() + 2, image.getHeight() + 2);
		//mapcanvas.drawImage(0, 0, image);
		//System.out.println("Map drawn");
		//mapcanvas.setPixel(0, 0, (byte) 0);
	}

	protected abstract void draw(MapView mapview, MapCanvas mapcanvas, Player player);

	protected void fill(int x, int y, int w, int h, boolean black) {
		for (int oX = 0; oX < w; ++oX) {
			for (int oY = 0; oY < h; ++oY) {
				setPixel(x + oX, y + oY, black);
			}
		}
	}

	protected MapFont getFont() {
		return font;
	}

	protected void setFont(MapFont mapfont) {
		font = mapfont;
	}

	protected void ctrStr(int x, int y, String text, boolean black) {
		str(x - getFont().getWidth(text) / 2, y, text, black);
	}

	protected void str(int x, int y, String text, boolean black) {
		int xStart = x;

		for (int i = 0; i < text.length(); ++i) {
			char ch = text.charAt(i);
			if (ch == 10) {
				x = xStart;
				y += font.getHeight() + 1;
			} else {
//				if (ch == 167) {
//					int sprite = text.indexOf(59, i);
//					if (sprite >= 0) {
//						try {
//							color = Byte.parseByte(text.substring(i + 1, sprite));
//							i = sprite;
//							continue;
//						} catch (NumberFormatException var12) {
//							;
//						}
//					}
//				}

				CharacterSprite var13 = font.getChar(text.charAt(i));

				for (int r = 0; r < font.getHeight(); ++r) {
					for (int c = 0; c < var13.getWidth(); ++c) {
						if (var13.get(r, c)) {
							setPixel(x + c, y + r, black);
						}
					}
				}

				x += var13.getWidth() + 1;
			}
		}
	}

	public void setPixel(int x, int y, boolean black) {
		if (!clip || x >= clipL && x < clipR && y >= clipT && y < clipB) {
			mapCanvas.setPixel(x, y, black ? 119 : MapPalette.TRANSPARENT);
		}
	}

	public void setClip(boolean clip) {
		this.clip = clip;
	}

	public void clipRect(int l, int t, int r, int b) {
		clipL = l;
		clipT = t;
		clipR = r;
		clipB = b;
	}

//	public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
//		return Arrays.<String>asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
//	}

	/**
	 * Inserts newline and formatting into a string to wrap it within the specified width.
	 */
	protected String wrapFormattedStringToWidth(String str, int wrapWidth) {
		int i = sizeStringToWidth(str, wrapWidth);
//		System.out.println("string width " + i);

		if (str.length() <= i) {
			return str;
		} else {
			String s = str.substring(0, i);
			char c0 = str.charAt(i);
			boolean flag = c0 == 32 || c0 == 10;
			String s1 = str.substring(i + (flag ? 1 : 0));
			return s + "\n" + wrapFormattedStringToWidth(s1, wrapWidth);
		}
	}

	/**
	 * Determines how many characters from the string will fit into the specified width.
	 */
	private int sizeStringToWidth(String str, int wrapWidth) {
		int i = str.length();
		int j = 0;
		int k = 0;
		int l = -1;

		for (; k < i; ++k) {
			char c0 = str.charAt(k);

			switch (c0) {
				case '\n':
					--k;
					break;

				case ' ':
					l = k;

				default:
					j += getFont().getChar(c0).getWidth() + 1;
					break;
			}

			if (c0 == 10) {
				++k;
				l = k;
				break;
			}

			if (j > wrapWidth) {
				break;
			}
		}

		return k != i && l != -1 && l < k ? l : k;
	}
}
