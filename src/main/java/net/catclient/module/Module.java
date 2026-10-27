package net.catclient.module;

import net.minecraft.client.MinecraftClient;

/** Base class for every feature (HUD or utility). */
public abstract class Module {
	public final String name;
	public final String description;
	public boolean enabled;
	/** RGB colour used by modules that set {@link #usesColor}. Right-click a module in the menu to change it. */
	public int color = Gfx.PALETTE[0];
	public boolean usesColor;

	protected Module(String name, String description, boolean enabled) {
		this.name = name;
		this.description = description;
		this.enabled = enabled;
	}

	/** Called every client tick (also while disabled - check {@link #enabled} yourself). */
	public void onTick(MinecraftClient mc) {
	}

	/** Called once when the module gets switched off, to undo any side effects. */
	public void onDisable(MinecraftClient mc) {
	}

	public void toggle() {
		enabled = !enabled;
		if (!enabled) {
			onDisable(MinecraftClient.getInstance());
		}
		Notifier.push(name + (enabled ? " enabled" : " disabled"));
	}

	public void nextColor() {
		int[] p = Gfx.PALETTE;
		int idx = 0;
		for (int i = 0; i < p.length; i++) {
			if (p[i] == color) {
				idx = i;
			}
		}
		color = p[(idx + 1) % p.length];
	}
}
