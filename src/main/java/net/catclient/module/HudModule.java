package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** A module that draws a movable box on the HUD. */
public abstract class HudModule extends Module {
	public int x;
	public int y;

	protected HudModule(String name, String description, boolean enabled, int x, int y) {
		super(name, description, enabled);
		this.x = x;
		this.y = y;
	}

	public abstract int width(MinecraftClient mc);

	public abstract int height(MinecraftClient mc);

	/** Draw at the given top-left position (already clamped to the screen). */
	public abstract void draw(DrawContext ctx, MinecraftClient mc, int x, int y);

	public int clampedX(MinecraftClient mc) {
		return Math.max(0, Math.min(x, mc.getWindow().getScaledWidth() - width(mc)));
	}

	public int clampedY(MinecraftClient mc) {
		return Math.max(0, Math.min(y, mc.getWindow().getScaledHeight() - height(mc)));
	}

	public final void render(DrawContext ctx, MinecraftClient mc) {
		if (!enabled) {
			return;
		}
		draw(ctx, mc, clampedX(mc), clampedY(mc));
	}
}
