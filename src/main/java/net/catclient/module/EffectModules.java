package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Visual modules. Everything here is built from vanilla client-side features
 * (glow outline, particles, sounds) so no Mixins or custom renderers are needed.
 */
public final class EffectModules {
	private EffectModules() {
	}

	// ------------------------------------------------------------------ helpers

	private static double rnd() {
		return ThreadLocalRandom.current().nextDouble() * 2 - 1; // -1..1
	}

	static void particle(MinecraftClient mc, ParticleEffect p, double x, double y, double z, double vx, double vy, double vz) {
		if (mc.world != null) {
			mc.world.addParticleClient(p, x, y, z, vx, vy, vz);
		}
	}

	static void dust(MinecraftClient mc, int rgb, float size, double x, double y, double z, double vx, double vy, double vz) {
		particle(mc, new DustParticleEffect(rgb, size), x, y, z, vx, vy, vz);
	}

	private static void sound(MinecraftClient mc, net.minecraft.sound.SoundEvent ev, float pitch, float volume) {
		mc.getSoundManager().play(PositionedSoundInstance.master(ev, pitch, volume));
	}

	// ------------------------------------------------------------------ modules

	/** Outline-glow every other player (visible through walls). */
	public static class Esp extends Module {
		public Esp() {
			super("ESP", "Glow outline on other players, visible through walls", false);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.world == null) {
				return;
			}
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (p != mc.player) {
					p.setGlowing(true);
				}
			}
		}

		@Override
		public void onDisable(MinecraftClient mc) {
			if (mc.world == null) {
				return;
			}
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (p != mc.player) {
					p.setGlowing(false);
				}
			}
		}
	}

	/** Glow on yourself (visible in third person). */
	public static class BodyGlow extends Module {
		public BodyGlow() {
			super("Body Glow", "Glow outline on your own character (F5 view)", false);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (enabled && mc.player != null) {
				mc.player.setGlowing(true);
			}
		}

		@Override
		public void onDisable(MinecraftClient mc) {
			if (mc.player != null) {
				mc.player.setGlowing(false);
			}
		}
	}

	/** Makes invisible players visible on the client. */
	public static class SeeInvisible extends Module {
		public SeeInvisible() {
			super("See Invisible", "Show players that have the invisibility effect", false);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.world == null) {
				return;
			}
			for (PlayerEntity p : mc.world.getPlayers()) {
				if (p != mc.player && p.isInvisible()) {
					p.setInvisible(false);
				}
			}
		}
	}

	/** Rings and ribbons of particles around your current target. */
	public static class TargetEsp extends Module {
		public TargetEsp() {
			super("Target ESP", "Rings and ribbons around your target", false);
			usesColor = true;
		}

		@Override
		public void onTick(MinecraftClient mc) {
			LivingEntity t = Stats.target;
			if (!enabled || mc.world == null || t == null || t.isRemoved()
					|| System.currentTimeMillis() - Stats.lastHit > 6000) {
				return;
			}
			double time = mc.world.getTime() * 0.2;
			double r = t.getWidth() * 0.8 + 0.25;
			double h = t.getHeight();
			// three orbiting ribbons moving up and down
			for (int k = 0; k < 3; k++) {
				double a = time * 2 + k * Math.PI * 2 / 3;
				double y = t.getY() + h * (0.5 + 0.5 * Math.sin(time + k));
				int c = Gfx.mix(color, 0xFFFFFF, k / 3f);
				dust(mc, c, 0.9f, t.getX() + Math.cos(a) * r, y, t.getZ() + Math.sin(a) * r, 0, 0, 0);
			}
			// ring on the ground
			for (int i = 0; i < 6; i++) {
				double a = time + i * Math.PI / 3;
				dust(mc, color, 0.6f, t.getX() + Math.cos(a) * r, t.getY() + 0.05, t.getZ() + Math.sin(a) * r, 0, 0, 0);
			}
		}
	}

	/** Glowing trail behind you while you move. */
	public static class Trails extends Module {
		public Trails() {
			super("Trails", "Glowing trail behind you", false);
			usesColor = true;
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.player == null) {
				return;
			}
			Vec3d v = mc.player.getVelocity();
			if (v.horizontalLengthSquared() < 0.0004) {
				return;
			}
			for (int i = 0; i < 2; i++) {
				double back = ThreadLocalRandom.current().nextDouble() * 3;
				int c = Gfx.mix(color, 0xFFFFFF, (float) ThreadLocalRandom.current().nextDouble() * 0.5f);
				dust(mc, c, 1.0f,
						mc.player.getX() - v.x * back + rnd() * 0.15,
						mc.player.getY() + 0.15 + ThreadLocalRandom.current().nextDouble() * 0.3,
						mc.player.getZ() - v.z * back + rnd() * 0.15, 0, 0, 0);
			}
		}
	}

	/** Particle burst on every hit. */
	public static class HitParticles extends Module implements CombatListener {
		public HitParticles() {
			super("Hit Particles", "Particle burst when you hit something", false);
			usesColor = true;
		}

		@Override
		public void onHit(MinecraftClient mc, Entity t) {
			double y = t.getY() + t.getHeight() * 0.6;
			for (int i = 0; i < 14; i++) {
				dust(mc, color, 1.0f, t.getX() + rnd() * 0.3, y + rnd() * 0.3, t.getZ() + rnd() * 0.3,
						rnd() * 0.15, rnd() * 0.15, rnd() * 0.15);
			}
			for (int i = 0; i < 6; i++) {
				particle(mc, ParticleTypes.ENCHANTED_HIT, t.getX(), y, t.getZ(), rnd() * 0.3, rnd() * 0.3, rnd() * 0.3);
			}
		}
	}

	/** Big effect when your target dies. */
	public static class KillEffects extends Module implements CombatListener {
		public KillEffects() {
			super("Kill Effects", "Burst + sound when you kill someone", false);
			usesColor = true;
		}

		@Override
		public void onKill(MinecraftClient mc, LivingEntity t) {
			double y = t.getY() + t.getHeight() / 2;
			for (int i = 0; i < 30; i++) {
				particle(mc, ParticleTypes.TOTEM_OF_UNDYING, t.getX(), y, t.getZ(), rnd() * 0.5, rnd() * 0.5, rnd() * 0.5);
			}
			for (int i = 0; i < 40; i++) {
				dust(mc, color, 1.2f, t.getX(), y, t.getZ(), rnd() * 0.25, rnd() * 0.25, rnd() * 0.25);
			}
			sound(mc, SoundEvents.ENTITY_PLAYER_LEVELUP, 0.8f, 0.8f);
		}
	}

	/** Glowing shards floating around you and bursting on hits / kills. */
	public static class StarGlow extends Module implements CombatListener {
		public StarGlow() {
			super("Star Glow", "Glowing shards around you, bursts on hit and kill", false);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.player == null || mc.world.getTime() % 2 != 0) {
				return;
			}
			double a = ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
			double r = 0.8 + ThreadLocalRandom.current().nextDouble() * 0.7;
			particle(mc, ParticleTypes.END_ROD,
					mc.player.getX() + Math.cos(a) * r,
					mc.player.getY() + ThreadLocalRandom.current().nextDouble() * 2,
					mc.player.getZ() + Math.sin(a) * r,
					0, 0.01, 0);
		}

		@Override
		public void onHit(MinecraftClient mc, Entity t) {
			for (int i = 0; i < 10; i++) {
				particle(mc, ParticleTypes.END_ROD, t.getX(), t.getY() + t.getHeight() * 0.6, t.getZ(),
						rnd() * 0.2, rnd() * 0.2, rnd() * 0.2);
			}
		}

		@Override
		public void onKill(MinecraftClient mc, LivingEntity t) {
			for (int i = 0; i < 40; i++) {
				particle(mc, ParticleTypes.END_ROD, t.getX(), t.getY() + t.getHeight() / 2, t.getZ(),
						rnd() * 0.35, rnd() * 0.35, rnd() * 0.35);
			}
		}
	}

	/** Coloured particles floating around the world near you. */
	public static class WorldParticles extends Module {
		public WorldParticles() {
			super("World Particles", "Floating coloured particles around you", false);
			usesColor = true;
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.player == null) {
				return;
			}
			for (int i = 0; i < 2; i++) {
				int c = Gfx.mix(color, 0xFFFFFF, (float) ThreadLocalRandom.current().nextDouble() * 0.6f);
				dust(mc, c, 1.0f,
						mc.player.getX() + rnd() * 10, mc.player.getY() + rnd() * 4 + 2, mc.player.getZ() + rnd() * 10,
						0, 0.02, 0);
			}
		}
	}

	/** Falling snow / ash style atmosphere. */
	public static class Atmosphere extends Module {
		public Atmosphere() {
			super("Atmosphere", "Falling snowflakes around you", false);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.player == null) {
				return;
			}
			for (int i = 0; i < 3; i++) {
				particle(mc, ParticleTypes.SNOWFLAKE,
						mc.player.getX() + rnd() * 14, mc.player.getY() + 4 + rnd() * 4, mc.player.getZ() + rnd() * 14,
						rnd() * 0.01, -0.03, rnd() * 0.01);
			}
		}
	}

	/** Particle ring showing the 3-block attack radius. */
	public static class HitRange extends Module {
		public HitRange() {
			super("Hit Range", "Ring on the ground at 3 blocks (attack range)", false);
			usesColor = true;
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.player == null || mc.world.getTime() % 5 != 0) {
				return;
			}
			for (int i = 0; i < 24; i++) {
				double a = i * Math.PI * 2 / 24;
				dust(mc, color, 0.6f, mc.player.getX() + Math.cos(a) * 3.0, mc.player.getY() + 0.05,
						mc.player.getZ() + Math.sin(a) * 3.0, 0, 0, 0);
			}
		}
	}

	/** Plays a sound when you hit something. */
	public static class HitSounds extends Module implements CombatListener {
		public HitSounds() {
			super("HitSounds", "Sound when you hit something", false);
		}

		@Override
		public void onHit(MinecraftClient mc, Entity target) {
			sound(mc, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.4f, 0.8f);
		}
	}
}
