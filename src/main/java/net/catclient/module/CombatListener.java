package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** Implement on a module to get called when you hit or kill something (only while enabled). */
public interface CombatListener {
	default void onHit(MinecraftClient mc, Entity target) {
	}

	default void onKill(MinecraftClient mc, LivingEntity target) {
	}
}
