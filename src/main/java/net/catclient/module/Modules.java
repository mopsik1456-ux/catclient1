package net.catclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Direction;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Registry of every module. Add your own here. */
public final class Modules {
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

	public static final Overlays.Crosshair CROSSHAIR = new Overlays.Crosshair();

	public static final List<Module> ALL = List.of(
			// --- HUD ---
			new TextModule("FPS", "Frames per second", true, 4, 4, "FPS", mc -> String.valueOf(mc.getCurrentFps())),
			new TextModule("CPS", "Clicks per second (left | right)", true, 4, 20, "CPS",
					mc -> Stats.leftCps() + " | " + Stats.rightCps()),
			new TextModule("Ping", "Latency to the server", true, 4, 36, "Ping", mc -> {
				if (mc.player == null || mc.getNetworkHandler() == null) {
					return "-";
				}
				var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
				return entry == null ? "-" : entry.getLatency() + " ms";
			}),
			new TextModule("Coordinates", "XYZ position", true, 4, 52, "XYZ", mc -> mc.player == null ? "-"
					: String.format(Locale.ROOT, "%d %d %d", (int) Math.floor(mc.player.getX()),
					(int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()))),
			new TextModule("Direction", "Facing direction", false, 4, 68, "Dir", mc -> {
				if (mc.player == null) {
					return "-";
				}
				Direction d = mc.player.getHorizontalFacing();
				String s = d.asString();
				return Character.toUpperCase(s.charAt(0)) + s.substring(1);
			}),
			new TextModule("Combo", "Hits in a row without taking damage", true, 4, 84, "Combo",
					mc -> String.valueOf(Stats.combo)),
			new TextModule("Reach", "Distance of your last hit", true, 4, 100, "Reach",
					mc -> System.currentTimeMillis() - Stats.reachTime > 5000 ? "-"
							: String.format(Locale.ROOT, "%.2f", Stats.reach)),
			new TextModule("Hit Counter", "Total hits and accuracy", false, 4, 116, "Hits",
					mc -> Stats.hits + " (" + Stats.accuracy() + "%)"),
			new TextModule("Clock", "Real time", false, 4, 132, "Time", mc -> LocalTime.now().format(CLOCK)),
			new TextModule("Memory", "Java memory usage", false, 4, 148, "RAM", mc -> {
				Runtime r = Runtime.getRuntime();
				return (r.totalMemory() - r.freeMemory()) / 1048576 + "/" + r.maxMemory() / 1048576 + " MB";
			}),
			new KeystrokesModule(4, 168),
			new EffectsModule(90, 4),
			new CooldownsModule(210, 4),
			new ArmorModule(600, 60),
			new ItemCounterModule(250, 200),
			new PvpHud.TargetHud(250, 120),
			new PvpHud.CombatStats(4, 250),
			new PvpHud.MaceHelper(250, 160),
			CROSSHAIR,
			new Overlays.LowHp(),
			new Overlays.DynamicIsland(),
			// --- visuals ---
			new EffectModules.Esp(),
			new EffectModules.BodyGlow(),
			new EffectModules.SeeInvisible(),
			new EffectModules.TargetEsp(),
			new EffectModules.Trails(),
			new EffectModules.HitParticles(),
			new EffectModules.KillEffects(),
			new EffectModules.StarGlow(),
			new EffectModules.WorldParticles(),
			new EffectModules.Atmosphere(),
			new EffectModules.HitRange(),
			new EffectModules.HitSounds(),
			// --- utility ---
			new UtilityModules.Zoom(),
			new UtilityModules.ToggleSprint(),
			new UtilityModules.Fullbright()
	);

	private Modules() {
	}

	public static void tick(MinecraftClient mc) {
		for (Module m : ALL) {
			m.onTick(mc);
		}
	}
}
