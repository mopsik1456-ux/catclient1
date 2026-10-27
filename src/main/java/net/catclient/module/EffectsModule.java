package net.catclient.module;

import net.catclient.gui.MenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

/** Active potion effects with level and time left. */
public class EffectsModule extends HudModule {
	private static final String[] ROMAN = {"", "", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

	public EffectsModule(int x, int y) {
		super("Potion Effects", "Active effects, level and time left", true, x, y);
	}

	private List<StatusEffectInstance> effects(MinecraftClient mc) {
		List<StatusEffectInstance> list = new ArrayList<>();
		if (mc.player == null) {
			return list;
		}
		for (StatusEffectInstance e : mc.player.getStatusEffects()) {
			list.add(e);
		}
		return list;
	}

	@Override
	public int width(MinecraftClient mc) {
		return 112;
	}

	@Override
	public int height(MinecraftClient mc) {
		return Math.max(1, effects(mc).size()) * 12 + 2;
	}

	@Override
	public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
		List<StatusEffectInstance> list = effects(mc);
		if (list.isEmpty()) {
			if (mc.currentScreen instanceof MenuScreen) {
				Gfx.panel(ctx, x, y, width(mc), height(mc));
				Gfx.text(ctx, mc.textRenderer, "Potion Effects", x + 6, y + 4, Gfx.GRAY);
			}
			return;
		}
		Gfx.panel(ctx, x, y, width(mc), height(mc));
		int yy = y + 2;
		for (StatusEffectInstance e : list) {
			ctx.fill(x + 5, yy + 2, x + 11, yy + 8, 0xFF000000 | e.getEffectType().value().getColor());
			int amp = e.getAmplifier() + 1;
			String name = e.getEffectType().value().getName().getString()
					+ (amp > 1 ? " " + (amp < ROMAN.length ? ROMAN[amp] : String.valueOf(amp)) : "");
			Gfx.text(ctx, mc.textRenderer, name, x + 15, yy + 1, Gfx.WHITE);
			String time = e.isInfinite() ? "**" : Gfx.time(e.getDuration() / 20);
			int color = !e.isInfinite() && e.getDuration() < 200 ? Gfx.RED : Gfx.GRAY;
			Gfx.text(ctx, mc.textRenderer, time, x + width(mc) - mc.textRenderer.getWidth(time) - 4, yy + 1, color);
			yy += 12;
		}
	}
}
