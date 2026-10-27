package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** A module that draws over the whole screen (not a movable box). */
public interface Overlay {
	void drawOverlay(DrawContext ctx, MinecraftClient mc);
}
