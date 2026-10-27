package net.catclient.module;

import net.catclient.gui.MenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemCooldownManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Item cooldowns (ender pearl, chorus fruit, shield, golden apple, ...). */
public class CooldownsModule extends HudModule {
	/** Item -> full cooldown in ticks (0 = unknown, shows a percent instead of seconds). */
	private static final Map<Item, Integer> TRACKED = new LinkedHashMap<>();

	static {
		TRACKED.put(Items.ENDER_PEARL, 20);
		TRACKED.put(Items.CHORUS_FRUIT, 20);
		TRACKED.put(Items.WIND_CHARGE, 10);
		TRACKED.put(Items.SHIELD, 100);
		TRACKED.put(Items.GOLDEN_APPLE, 0);
		TRACKED.put(Items.ENCHANTED_GOLDEN_APPLE, 0);
		TRACKED.put(Items.TOTEM_OF_UNDYING, 0);
		TRACKED.put(Items.SNOWBALL, 0);
		TRACKED.put(Items.EGG, 0);
	}

	private record Entry(ItemStack stack, float progress, int totalTicks) {
	}

	public CooldownsModule(int x, int y) {
		super("Cooldowns", "Ender pearl, shield, apples and other item cooldowns", true, x, y);
	}

	private List<Entry> active(MinecraftClient mc) {
		List<Entry> list = new ArrayList<>();
		if (mc.player == null) {
			return list;
		}
		ItemCooldownManager m = mc.player.getItemCooldownManager();
		for (Map.Entry<Item, Integer> e : TRACKED.entrySet()) {
			ItemStack s = new ItemStack(e.getKey());
			float p = m.getCooldownProgress(s, 0f);
			if (p > 0f) {
				list.add(new Entry(s, p, e.getValue()));
			}
		}
		return list;
	}

	@Override
	public int width(MinecraftClient mc) {
		return 92;
	}

	@Override
	public int height(MinecraftClient mc) {
		return Math.max(1, active(mc).size()) * 18 + 2;
	}

	@Override
	public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
		List<Entry> list = active(mc);
		if (list.isEmpty()) {
			if (mc.currentScreen instanceof MenuScreen) { // preview while editing
				list = List.of(new Entry(new ItemStack(Items.ENDER_PEARL), 0.6f, 20));
			} else {
				return;
			}
		}
		Gfx.panel(ctx, x, y, width(mc), list.size() * 18 + 2);
		int yy = y + 2;
		for (Entry e : list) {
			ctx.drawItem(e.stack(), x + 4, yy);
			int bx = x + 24;
			int bw = 62;
			ctx.fill(bx, yy + 11, bx + bw, yy + 14, 0xFF303030);
			ctx.fill(bx, yy + 11, bx + (int) (bw * e.progress()), yy + 14, Gfx.ACCENT);
			String t = e.totalTicks() > 0
					? String.format("%.1fs", e.progress() * e.totalTicks() / 20f)
					: (int) (e.progress() * 100) + "%";
			Gfx.text(ctx, mc.textRenderer, t, bx, yy + 1, Gfx.WHITE);
			yy += 18;
		}
	}
}
