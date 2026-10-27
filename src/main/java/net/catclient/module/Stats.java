package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

import java.util.ArrayDeque;

/** Shared runtime state: CPS counters, combo, reach, hits, kills, deaths and the current target. */
public final class Stats {
	private static final ArrayDeque<Long> LEFT = new ArrayDeque<>();
	private static final ArrayDeque<Long> RIGHT = new ArrayDeque<>();
	private static boolean leftDown;
	private static boolean rightDown;

	public static int combo;
	public static int hits;
	public static int swings;
	public static int kills;
	public static int deaths;
	public static long lastHit;
	public static double reach;
	public static long reachTime;
	public static LivingEntity target;

	private static int lastHurt;
	private static boolean killCounted;
	private static boolean wasDead;

	private Stats() {
	}

	/** Polled every rendered frame so fast clicks are not missed. */
	public static void pollMouse(boolean left, boolean right, boolean inGame) {
		long now = System.currentTimeMillis();
		if (left && !leftDown) {
			LEFT.addLast(now);
			if (inGame) {
				swings++;
			}
		}
		if (right && !rightDown) {
			RIGHT.addLast(now);
		}
		leftDown = left;
		rightDown = right;
		prune(LEFT, now);
		prune(RIGHT, now);
	}

	private static void prune(ArrayDeque<Long> q, long now) {
		while (!q.isEmpty() && now - q.peekFirst() > 1000) {
			q.removeFirst();
		}
	}

	public static int leftCps() {
		prune(LEFT, System.currentTimeMillis());
		return LEFT.size();
	}

	public static int rightCps() {
		prune(RIGHT, System.currentTimeMillis());
		return RIGHT.size();
	}

	public static int accuracy() {
		return swings == 0 ? 0 : Math.min(100, hits * 100 / swings);
	}

	public static void onAttack(MinecraftClient mc, Entity entity) {
		long now = System.currentTimeMillis();
		combo++;
		hits++;
		lastHit = now;
		if (entity instanceof LivingEntity le) {
			target = le;
			killCounted = false;
		}
		if (mc.crosshairTarget instanceof EntityHitResult r && mc.player != null) {
			reach = mc.player.getEyePos().distanceTo(r.getPos());
		} else if (mc.player != null) {
			reach = mc.player.distanceTo(entity);
		}
		reachTime = now;
		for (Module m : Modules.ALL) {
			if (m.enabled && m instanceof CombatListener l) {
				l.onHit(mc, entity);
			}
		}
	}

	public static void tick(MinecraftClient mc) {
		if (mc.player == null) {
			combo = 0;
			target = null;
			return;
		}
		long now = System.currentTimeMillis();

		int hurt = mc.player.hurtTime;
		if (hurt > 0 && lastHurt == 0) {
			combo = 0; // we got hit -> combo broken
		}
		lastHurt = hurt;
		if (combo > 0 && now - lastHit > 4000) {
			combo = 0;
		}

		boolean dead = mc.player.isDead();
		if (dead && !wasDead) {
			deaths++;
		}
		wasDead = dead;

		if (target != null) {
			boolean gone = target.isDead() || target.isRemoved() || target.getHealth() <= 0;
			if (gone && !killCounted && now - lastHit < 5000) {
				killCounted = true;
				kills++;
				Notifier.push("Kill: " + target.getName().getString());
				for (Module m : Modules.ALL) {
					if (m.enabled && m instanceof CombatListener l) {
						l.onKill(mc, target);
					}
				}
				target = null;
			} else if (now - lastHit > 8000) {
				target = null;
			}
		}
	}
}
