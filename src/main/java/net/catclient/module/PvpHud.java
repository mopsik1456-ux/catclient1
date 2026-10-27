package net.catclient.module;

import net.catclient.gui.MenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Locale;

/** PvP information boxes: Target HUD, Combat Stats, Mace Helper. */
public final class PvpHud {
	private PvpHud() {
	}

	public static class TargetHud extends HudModule {
		public TargetHud(int x, int y) {
			super("Target HUD", "Name, health and armor of your target", true, x, y);
		}

		private LivingEntity current(MinecraftClient mc) {
			if (Stats.target != null && !Stats.target.isRemoved() && System.currentTimeMillis() - Stats.lastHit < 6000) {
				return Stats.target;
			}
			if (mc.crosshairTarget instanceof EntityHitResult r && r.getEntity() instanceof LivingEntity le) {
				return le;
			}
			return null;
		}

		@Override
		public int width(MinecraftClient mc) {
			return 124;
		}

		@Override
		public int height(MinecraftClient mc) {
			return 34;
		}

		@Override
		public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
			LivingEntity t = current(mc);
			boolean preview = false;
			if (t == null) {
				if (!(mc.currentScreen instanceof MenuScreen)) {
					return;
				}
				preview = true;
			}
			String name = preview ? "Steve" : t.getName().getString();
			float hp = preview ? 20f : t.getHealth();
			float max = preview ? 20f : t.getMaxHealth();
			int armor = preview ? 20 : t.getArmor();

			Gfx.panel(ctx, x, y, width(mc), height(mc));
			Gfx.text(ctx, mc.textRenderer, name, x + 6, y + 4, Gfx.WHITE);
			float ratio = max <= 0 ? 0 : Math.max(0f, Math.min(1f, hp / max));
			ctx.fill(x + 6, y + 15, x + 118, y + 21, 0xFF303030);
			ctx.fill(x + 6, y + 15, x + 6 + (int) (112 * ratio), y + 21, 0xFF000000 | Gfx.mix(0xFF5555, 0x55FF55, ratio));
			Gfx.text(ctx, mc.textRenderer, String.format(Locale.ROOT, "%.1f / %.0f  armor %d", hp, max, armor),
					x + 6, y + 24, Gfx.GRAY);
		}
	}

	public static class CombatStats extends HudModule {
		public CombatStats(int x, int y) {
			super("Combat Stats", "Kills, deaths, K/D, hits and accuracy", false, x, y);
		}

		@Override
		public int width(MinecraftClient mc) {
			return 100;
		}

		@Override
		public int height(MinecraftClient mc) {
			return 50;
		}

		@Override
		public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
			Gfx.panel(ctx, x, y, width(mc), height(mc));
			double kd = Stats.deaths == 0 ? Stats.kills : (double) Stats.kills / Stats.deaths;
			line(ctx, mc, x, y + 4, "Kills", String.valueOf(Stats.kills));
			line(ctx, mc, x, y + 14, "Deaths", String.valueOf(Stats.deaths));
			line(ctx, mc, x, y + 24, "K/D", String.format(Locale.ROOT, "%.2f", kd));
			line(ctx, mc, x, y + 34, "Hits", Stats.hits + " (" + Stats.accuracy() + "%)");
		}

		private void line(DrawContext ctx, MinecraftClient mc, int x, int y, String label, String value) {
			Gfx.text(ctx, mc.textRenderer, label, x + 6, y, Gfx.ACCENT);
			Gfx.text(ctx, mc.textRenderer, value, x + 94 - mc.textRenderer.getWidth(value), y, Gfx.WHITE);
		}
	}

	/** Shows fall distance, expected smash damage and weapon charge while holding a mace. */
	public static class MaceHelper extends HudModule {
		public MaceHelper(int x, int y) {
			super("Mace Helper", "Fall distance, smash damage and charge while holding a mace", false, x, y);
		}

		@Override
		public int width(MinecraftClient mc) {
			return 100;
		}

		@Override
		public int height(MinecraftClient mc) {
			return 36;
		}

		@Override
		public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
			boolean preview = mc.currentScreen instanceof MenuScreen;
			if (mc.player == null || (!mc.player.getMainHandStack().isOf(Items.MACE) && !preview)) {
				return;
			}
			double fd = mc.player == null ? 6.0 : mc.player.fallDistance;
			double bonus = fd <= 3 ? fd * 4 : fd <= 8 ? 12 + (fd - 3) * 2 : 22 + (fd - 8);
			double dmg = fd < 1.5 ? 0 : 6 + bonus;
			float charge = mc.player == null ? 1f : mc.player.getAttackCooldownProgress(0f);

			Gfx.panel(ctx, x, y, width(mc), height(mc));
			Gfx.text(ctx, mc.textRenderer, String.format(Locale.ROOT, "Fall %.1f m", fd), x + 6, y + 4, Gfx.WHITE);
			Gfx.text(ctx, mc.textRenderer, dmg == 0 ? "Smash: -" : String.format(Locale.ROOT, "Smash ~%.0f dmg", dmg),
					x + 6, y + 14, dmg == 0 ? Gfx.GRAY : Gfx.YELLOW);
			ctx.fill(x + 6, y + 26, x + 94, y + 30, 0xFF303030);
			ctx.fill(x + 6, y + 26, x + 6 + (int) (88 * charge), y + 30, charge >= 1f ? Gfx.GREEN : Gfx.ACCENT);
		}
	}
}
