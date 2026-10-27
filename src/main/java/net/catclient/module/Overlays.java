package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.EntityHitResult;

/** Full-screen overlays: Low HP warning, Dynamic Island, custom Crosshair. */
public final class Overlays {
	private Overlays() {
	}

	/** Red pulsing vignette when your health is low. */
	public static class LowHp extends Module implements Overlay {
		private boolean warned;

		public LowHp() {
			super("Low HP", "Red warning on screen when health is low", true);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (mc.player == null) {
				return;
			}
			boolean low = mc.player.getHealth() <= 6f && !mc.player.isDead();
			if (low && !warned && enabled) {
				Notifier.push("Low HP!");
			}
			warned = low;
		}

		@Override
		public void drawOverlay(DrawContext ctx, MinecraftClient mc) {
			if (mc.player == null || mc.player.getHealth() > 6f || mc.player.isDead()) {
				return;
			}
			int w = mc.getWindow().getScaledWidth();
			int h = mc.getWindow().getScaledHeight();
			double pulse = 0.65 + 0.35 * Math.sin(System.currentTimeMillis() / 180.0);
			int steps = 14;
			for (int i = 0; i < steps; i++) {
				int a = (int) (110 * pulse * (1 - i / (float) steps));
				Gfx.outline(ctx, i, i, w - i * 2, h - i * 2, (a << 24) | 0xFF0000);
			}
		}
	}

	/** Pill at the top of the screen that shows short notifications (toggles, kills, low HP). */
	public static class DynamicIsland extends Module implements Overlay {
		public DynamicIsland() {
			super("Dynamic Island", "Notification pill at the top of the screen", true);
		}

		@Override
		public void drawOverlay(DrawContext ctx, MinecraftClient mc) {
			long age = System.currentTimeMillis() - Notifier.time;
			if (Notifier.text.isEmpty() || age > 2800) {
				return;
			}
			// slide in during the first 200 ms, slide out during the last 300 ms
			float slide = age < 200 ? age / 200f : age > 2500 ? (2800 - age) / 300f : 1f;
			int tw = mc.textRenderer.getWidth(Notifier.text);
			int w = tw + 28;
			int h = 16;
			int x = (mc.getWindow().getScaledWidth() - w) / 2;
			int y = (int) (-h + (6 + h) * slide);
			ctx.fill(x + 3, y, x + w - 3, y + h, 0xFF000000);
			ctx.fill(x, y + 3, x + w, y + h - 3, 0xFF000000);
			ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF000000);
			ctx.fill(x + 8, y + 6, x + 12, y + 10, 0xFF000000 | Gfx.ACCENT);
			Gfx.text(ctx, mc.textRenderer, Notifier.text, x + 17, y + 4, Gfx.WHITE);
		}
	}

	/** Replaces the vanilla crosshair with a coloured one (turns red over entities). */
	public static class Crosshair extends Module {
		public Crosshair() {
			super("Crosshair", "Custom coloured crosshair (replaces vanilla)", false);
			usesColor = true;
		}

		public void draw(DrawContext ctx, MinecraftClient mc) {
			if (mc.options.getPerspective() != net.minecraft.client.option.Perspective.FIRST_PERSON) {
				return;
			}
			int cx = mc.getWindow().getScaledWidth() / 2;
			int cy = mc.getWindow().getScaledHeight() / 2;
			int c = 0xFF000000 | (mc.crosshairTarget instanceof EntityHitResult ? 0xFF5555 : color);
			int gap = 2;
			int len = 5;
			ctx.fill(cx - gap - len, cy, cx - gap, cy + 1, c);
			ctx.fill(cx + gap + 1, cy, cx + gap + 1 + len, cy + 1, c);
			ctx.fill(cx, cy - gap - len, cx + 1, cy - gap, c);
			ctx.fill(cx, cy + gap + 1, cx + 1, cy + gap + 1 + len, c);
		}
	}
}
