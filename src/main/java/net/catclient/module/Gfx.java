package net.catclient.module;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Small drawing helpers shared by all HUD modules. */
public final class Gfx {
	public static final int ACCENT = 0xFFFF9F43;
	public static final int BG = 0x90101010;
	public static final int WHITE = 0xFFFFFFFF;
	public static final int GRAY = 0xFFAAAAAA;
	public static final int RED = 0xFFFF5555;
	public static final int YELLOW = 0xFFFFFF55;
	public static final int GREEN = 0xFF55FF55;

	/** RGB colours you can cycle through with right-click in the menu. */
	public static final int[] PALETTE = {0xFF9F43, 0xFF5555, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF, 0xFFFFFF, 0xFFFF55};

	private Gfx() {
	}

	public static int mix(int a, int b, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
		int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
		int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
		return (r << 16) | (g << 8) | bl;
	}

	/** Dark translucent panel with an accent stripe on the left. */
	public static void panel(DrawContext c, int x, int y, int w, int h) {
		c.fill(x, y, x + w, y + h, BG);
		c.fill(x, y, x + 2, y + h, ACCENT);
	}

	public static void text(DrawContext c, TextRenderer tr, String s, int x, int y, int color) {
		c.drawTextWithShadow(tr, s, x, y, color);
	}

	public static void centered(DrawContext c, TextRenderer tr, String s, int cx, int y, int color) {
		c.drawTextWithShadow(tr, s, cx - tr.getWidth(s) / 2, y, color);
	}

	public static void outline(DrawContext c, int x, int y, int w, int h, int color) {
		c.fill(x, y, x + w, y + 1, color);
		c.fill(x, y + h - 1, x + w, y + h, color);
		c.fill(x, y, x + 1, y + h, color);
		c.fill(x + w - 1, y, x + w, y + h, color);
	}

	public static String time(int seconds) {
		return String.format("%d:%02d", seconds / 60, seconds % 60);
	}
}
