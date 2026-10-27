package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Armor pieces + main hand with durability. */
public class ArmorModule extends HudModule {
	private static final EquipmentSlot[] SLOTS = {
			EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND
	};

	public ArmorModule(int x, int y) {
		super("Armor Status", "Armor and held item durability", true, x, y);
	}

	private List<ItemStack> stacks(MinecraftClient mc) {
		List<ItemStack> list = new ArrayList<>();
		if (mc.player == null) {
			return list;
		}
		for (EquipmentSlot s : SLOTS) {
			ItemStack st = mc.player.getEquippedStack(s);
			if (!st.isEmpty()) {
				list.add(st);
			}
		}
		return list;
	}

	@Override
	public int width(MinecraftClient mc) {
		return 62;
	}

	@Override
	public int height(MinecraftClient mc) {
		return Math.max(1, stacks(mc).size()) * 18 + 2;
	}

	@Override
	public void draw(DrawContext ctx, MinecraftClient mc, int x, int y) {
		List<ItemStack> list = stacks(mc);
		Gfx.panel(ctx, x, y, width(mc), height(mc));
		if (list.isEmpty()) {
			Gfx.text(ctx, mc.textRenderer, "No armor", x + 6, y + 6, Gfx.GRAY);
			return;
		}
		int yy = y + 2;
		for (ItemStack st : list) {
			ctx.drawItem(st, x + 4, yy);
			String text;
			int color = Gfx.WHITE;
			if (st.isDamageable()) {
				int left = st.getMaxDamage() - st.getDamage();
				float f = left / (float) st.getMaxDamage();
				text = String.valueOf(left);
				color = f > 0.6f ? Gfx.GREEN : f > 0.3f ? Gfx.YELLOW : Gfx.RED;
			} else {
				text = st.getCount() > 1 ? "x" + st.getCount() : "";
			}
			Gfx.text(ctx, mc.textRenderer, text, x + 24, yy + 4, color);
			yy += 18;
		}
	}
}
