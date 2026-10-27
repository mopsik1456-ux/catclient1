package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Function;

/** Generic one-line "Label: value" box (FPS, CPS, Ping, Coords, ...). */
public class TextModule extends HudModule {
	private final String label;
	private final Function<MinecraftClient, String> value;

	public TextModule(String name, String description, boolean enabled, int x, int y,
					  String label, Function<MinecraftClient, String> value) {
		super(name, description, enabled, x, y);
		this.label = label;
		this.value = value;
	}

	private String valueOf(MinecraftClient mc) {
		try {
			return value.apply(mc);
		} catch (Exception e) {
			return "-";
		}
	}

	@Override
	public int width(MinecraftClient mc) {
		return mc.textRenderer.getWidth(label + " " + valueOf(mc)) + 8;
	}

	@Override
	public int height(MinecraftClient mc) {
		return 14;
	}

	@Override
	public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
		Gfx.panel(ctx, x, y, width(mc), height(mc));
		int lw = mc.textRenderer.getWidth(label + " ");
		Gfx.text(ctx, mc.textRenderer, label, x + 5, y + 3, Gfx.ACCENT);
		Gfx.text(ctx, mc.textRenderer, valueOf(mc), x + 5 + lw, y + 3, Gfx.WHITE);
	}
}
