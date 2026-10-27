package net.catclient;

import net.catclient.gui.MenuScreen;
import net.catclient.module.HudModule;
import net.catclient.module.Module;
import net.catclient.module.Modules;
import net.catclient.module.Overlay;
import net.catclient.module.Stats;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CatClient implements ClientModInitializer {
	public static final String MOD_ID = "catclient";

	public static KeyBinding menuKey;
	public static KeyBinding zoomKey;

	@Override
	public void onInitializeClient() {
		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));
		menuKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.catclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
		zoomKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key.catclient.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, category));

		Config.load();

		// logic
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (menuKey.wasPressed()) {
				if (mc.currentScreen == null) {
					mc.setScreen(new MenuScreen());
				}
			}
			Stats.tick(mc);
			Modules.tick(mc);
		});

		// HUD
		HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, Identifier.of(MOD_ID, "hud"), (ctx, tickCounter) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			long window = mc.getWindow().getHandle();
			Stats.pollMouse(
					GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS,
					GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS,
					mc.player != null && mc.currentScreen == null);
			if (mc.player == null || mc.options.hudHidden) {
				return;
			}
			for (Module m : Modules.ALL) {
				if (m instanceof HudModule h) {
					h.render(ctx, mc);
				}
			}
			for (Module m : Modules.ALL) {
				if (m.enabled && m instanceof Overlay o) {
					o.drawOverlay(ctx, mc);
				}
			}
		});

		// custom crosshair replaces the vanilla one only while the module is enabled
		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (ctx, tickCounter) -> {
			if (Modules.CROSSHAIR.enabled) {
				Modules.CROSSHAIR.draw(ctx, MinecraftClient.getInstance());
			} else {
				original.render(ctx, tickCounter);
			}
		});

		// combo + reach
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (world.isClient() && player == mc.player && entity instanceof LivingEntity) {
				Stats.onAttack(mc, entity);
			}
			return ActionResult.PASS;
		});
	}
}
