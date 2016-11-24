//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.amrsatrio.server.mapgui;

import java.util.HashMap;

public class MapFont {
	private final HashMap<Character, MapFont.CharacterSprite> chars = new HashMap<>();
	protected boolean malleable = true;
	private int height = 0;

	public void setChar(char ch, MapFont.CharacterSprite sprite) {
		if (!this.malleable) {
			throw new IllegalStateException("this font is not malleable");
		} else {
			this.chars.put(ch, sprite);
			if (sprite.getHeight() > this.height) {
				this.height = sprite.getHeight();
			}

		}
	}

	public MapFont.CharacterSprite getChar(char ch) {
		return this.chars.get(ch);
	}

	public int getWidth(String text) {
		if (!this.isValid(text)) {
			throw new IllegalArgumentException("text contains invalid characters");
		} else if (text.length() == 0) {
			return 0;
		} else {
			int result = 0;

			for (int i = 0; i < text.length(); ++i) {
				char ch = text.charAt(i);
//				if (ch != 167) {
				result += this.chars.get(ch).getWidth();
//				}
			}

			result += text.length() - 1;
			return result;
		}
	}

	public int getHeight() {
		return this.height;
	}

	public boolean isValid(String text) {
		for (int i = 0; i < text.length(); ++i) {
			char ch = text.charAt(i);
			if (ch != 167 && ch != 10 && this.chars.get(ch) == null) {
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
			return (row >= 0 && col >= 0 && row < this.height && col < this.width) && this.data[row * this.width + col];
		}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}
	}
}
