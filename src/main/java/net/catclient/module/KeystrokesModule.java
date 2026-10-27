package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public class KeystrokesModule extends HudModule {
	private static final int K = 20;
	private static final int GAP = 2;

	public KeystrokesModule(int x, int y) {
		super("Keystrokes", "WASD, jump and mouse buttons with live CPS", true, x, y);
	}

	@Override
	public int width(MinecraftClient mc) {
		return K * 3 + GAP * 2;
	}

	@Override
	public int height(MinecraftClient mc) {
		return K * 3 + GAP * 3 + 10;
	}

	@Override
	public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
		var o = mc.options;
		key(ctx, mc, x + K + GAP, y, K, K, "W", o.forwardKey);
		key(ctx, mc, x, y + K + GAP, K, K, "A", o.leftKey);
		key(ctx, mc, x + K + GAP, y + K + GAP, K, K, "S", o.backKey);
		key(ctx, mc, x + (K + GAP) * 2, y + K + GAP, K, K, "D", o.rightKey);
		int w = (K * 3 + GAP * 2 - GAP) / 2;
		int ry = y + (K + GAP) * 2;
		key(ctx, mc, x, ry, w, K, "LMB", o.attackKey, Stats.leftCps() + " CPS");
		key(ctx, mc, x + w + GAP, ry, w, K, "RMB", o.useKey, Stats.rightCps() + " CPS");
		key(ctx, mc, x, ry + K + GAP, K * 3 + GAP * 2, 10, "", o.jumpKey);
	}

	private void key(DrawContext ctx, MinecraftClient mc, int x, int y, int w, int h, String label, KeyBinding kb) {
		key(ctx, mc, x, y, w, h, label, kb, null);
	}

	private void key(DrawContext ctx, MinecraftClient mc, int x, int y, int w, int h,
					 String label, KeyBinding kb, String sub) {
		boolean down = kb.isPressed();
		ctx.fill(x, y, x + w, y + h, down ? 0xB0FFFFFF : Gfx.BG);
		int fg = down ? 0xFF000000 : Gfx.WHITE;
		if (h <= 10) {
			ctx.fill(x + 6, y + 4, x + w - 6, y + 5, fg); // space bar line
			return;
		}
		if (sub == null) {
			Gfx.centered(ctx, mc.textRenderer, label, x + w / 2, y + (h - 8) / 2, fg);
		} else {
			Gfx.centered(ctx, mc.textRenderer, label, x + w / 2, y + 2, fg);
			Gfx.centered(ctx, mc.textRenderer, sub, x + w / 2, y + 11, down ? 0xFF333333 : Gfx.GRAY);
		}
	}
}
