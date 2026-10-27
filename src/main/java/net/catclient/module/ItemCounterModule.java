package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** PvP item counter: totems, golden apples, pearls, splash potions. */
public class ItemCounterModule extends HudModule {
	private static final Item[] ITEMS = {
			Items.TOTEM_OF_UNDYING, Items.GOLDEN_APPLE, Items.ENDER_PEARL, Items.SPLASH_POTION
	};

	public ItemCounterModule(int x, int y) {
		super("PvP Items", "Counts totems, gapples, pearls and splash potions", true, x, y);
	}

	@Override
	public int width(MinecraftClient mc) {
		return ITEMS.length * 30 + 4;
	}

	@Override
	public int height(MinecraftClient mc) {
		return 20;
	}

	private int count(MinecraftClient mc, Item item) {
		if (mc.player == null) {
			return 0;
		}
		PlayerInventory inv = mc.player.getInventory();
		int total = 0;
		for (int i = 0; i < inv.size(); i++) {
			ItemStack s = inv.getStack(i);
			if (s.isOf(item)) {
				total += s.getCount();
			}
		}
		return total;
	}

	@Override
	public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
		Gfx.panel(ctx, x, y, width(mc), height(mc));
		int xx = x + 4;
		for (Item item : ITEMS) {
			ctx.drawItem(new ItemStack(item), xx, y + 2);
			int n = count(mc, item);
			Gfx.text(ctx, mc.textRenderer, String.valueOf(n), xx + 17, y + 6, n == 0 ? Gfx.RED : Gfx.WHITE);
			xx += 30;
		}
	}
}
