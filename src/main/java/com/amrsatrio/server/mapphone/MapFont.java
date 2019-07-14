//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.amrsatrio.server.mapphone;

import java.util.HashMap;

public class MapFont {
	private final HashMap<Character, CharacterSprite> chars = new HashMap<>();
	protected boolean malleable = true;
	private int height = 0;

	public void setChar(char ch, CharacterSprite sprite) {
		if (!malleable) {
			throw new IllegalStateException("this font is not malleable");
		} else {
			chars.put(ch, sprite);
			if (sprite.getHeight() > height) {
				height = sprite.getHeight();
			}
		}
	}

	public CharacterSprite getChar(char ch) {
		CharacterSprite charactersprite = chars.get(ch);
		return charactersprite == null ? chars.get('?') : charactersprite;
	}

	public int getWidth(String text) {
		if (!isValid(text)) {
			throw new IllegalArgumentException("text contains invalid characters");
		} else if (text.length() == 0) {
			return 0;
		} else {
			int result = 0;

			for (int i = 0; i < text.length(); ++i) {
				char ch = text.charAt(i);
//				if (ch != 167) {
				result += chars.get(ch).getWidth();
//				}
			}

			result += text.length() - 1;
			return result;
		}
	}

	public int getHeight() {
		return height;
	}

	public boolean isValid(String text) {
		for (int i = 0; i < text.length(); ++i) {
			char ch = text.charAt(i);
			if (ch != 167 && ch != 10 && chars.get(ch) == null) {
				return false;
			}
		}

		return true;
	}

	public static class CharacterSprite {
		private final int width;
		private final int height;
		private final boolean[] data;

		public CharacterSprite(int width, int height, boolean[] data) {
			this.width = width;
			this.height = height;
			this.data = data;
			if (data.length != width * height) {
				throw new IllegalArgumentException("size of data does not match dimensions");
			}
		}

		public boolean get(int row, int col) {
			return row >= 0 && col >= 0 && row < height && col < width && data[row * width + col];
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}
}
