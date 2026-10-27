package net.catclient.module;

import net.catclient.CatClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/** Non-HUD modules: Fullbright, Zoom, Toggle Sprint. */
public final class UtilityModules {
	private UtilityModules() {
	}

	/** Client-side night vision. */
	public static class Fullbright extends Module {
		public Fullbright() {
			super("Fullbright", "See in the dark (client-side night vision)", false);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (!enabled || mc.player == null) {
				return;
			}
			if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
				mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1_000_000, 0, false, false));
			}
		}

		@Override
		public void onDisable(MinecraftClient mc) {
			if (mc.player != null) {
				mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
			}
		}
	}

	/** Hold the zoom key (default C) to zoom in. */
	public static class Zoom extends Module {
		private boolean zooming;
		private int oldFov;
		private double oldSens;

		public Zoom() {
			super("Zoom", "Hold C to zoom (rebind in Controls)", true);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			boolean hold = enabled && mc.currentScreen == null && CatClient.zoomKey != null && CatClient.zoomKey.isPressed();
			if (hold && !zooming) {
				oldFov = mc.options.getFov().getValue();
				oldSens = mc.options.getMouseSensitivity().getValue();
				zooming = true;
			}
			if (hold) {
				mc.options.getFov().setValue(30);
				mc.options.getMouseSensitivity().setValue(oldSens * 0.35);
			} else if (zooming) {
				restore(mc);
			}
		}

		@Override
		public void onDisable(MinecraftClient mc) {
			if (zooming) {
				restore(mc);
			}
		}

		private void restore(MinecraftClient mc) {
			mc.options.getFov().setValue(oldFov);
			mc.options.getMouseSensitivity().setValue(oldSens);
			zooming = false;
		}
	}

	/** Keeps sprint held while you walk forward. */
	public static class ToggleSprint extends Module {
		public ToggleSprint() {
			super("Toggle Sprint", "Automatically sprint while moving forward", true);
		}

		@Override
		public void onTick(MinecraftClient mc) {
			if (enabled && mc.player != null && mc.currentScreen == null
					&& mc.options.forwardKey.isPressed() && !mc.player.isSneaking()) {
				mc.options.sprintKey.setPressed(true);
			}
		}

		@Override
		public void onDisable(MinecraftClient mc) {
			mc.options.sprintKey.setPressed(false);
		}
	}
}
