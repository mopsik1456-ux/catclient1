package net.catclient.gui;

import net.catclient.Config;
import net.catclient.module.Gfx;
import net.catclient.module.HudModule;
import net.catclient.module.Module;
import net.catclient.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Cat Client menu: click a module to toggle it, drag any HUD box to move it.
 * Mouse input is polled with GLFW so the screen does not depend on Screen mouse-event signatures.
 */
public class MenuScreen extends Screen {
	private static final int COLS = 3;
	private static final int COL_W = 132;
	private static final int ROW_H = 16;

	private boolean wasDown;
	private boolean wasRightDown;
	private HudModule dragging;
	private int dragDx;
	private int dragDy;

	public MenuScreen() {
		super(Text.literal("Cat Client"));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		Config.save();
		super.close();
	}

	private int panelW() {
		return COLS * COL_W + 12;
	}

	private int panelH() {
		int rows = (Modules.ALL.size() + COLS - 1) / COLS;
		return 36 + rows * ROW_H + 18;
	}

	private int panelX() {
		return (width - panelW()) / 2;
	}

	private int panelY() {
		return (height - panelH()) / 2;
	}

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		List<Module> all = Modules.ALL;

		ctx.fill(0, 0, width, height, 0x55000000);

		// ---- input (polled) ----
		boolean down = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		boolean pressed = down && !wasDown;
		boolean released = !down && wasDown;
		wasDown = down;
		boolean rightDown = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		boolean rightPressed = rightDown && !wasRightDown;
		wasRightDown = rightDown;

		int px = panelX();
		int py = panelY();
		boolean inPanel = mx >= px && mx < px + panelW() && my >= py && my < py + panelH();

		if (rightPressed && inPanel) {
			Module hit = moduleAt(mx, my);
			if (hit != null && hit.usesColor) {
				hit.nextColor();
				Config.save();
			}
		}

		if (pressed) {
			if (inPanel) {
				Module hit = moduleAt(mx, my);
				if (hit != null) {
					hit.toggle();
					Config.save();
				}
			} else {
				for (int i = Modules.ALL.size() - 1; i >= 0; i--) {
					if (Modules.ALL.get(i) instanceof HudModule h && h.enabled && hits(mc, h, mx, my)) {
						dragging = h;
						dragDx = mx - h.clampedX(mc);
						dragDy = my - h.clampedY(mc);
						break;
					}
				}
			}
		}
		if (dragging != null && down) {
			dragging.x = Math.max(0, Math.min(mx - dragDx, width - dragging.width(mc)));
			dragging.y = Math.max(0, Math.min(my - dragDy, height - dragging.height(mc)));
		}
		if (released) {
			if (dragging != null) {
				Config.save();
			}
			dragging = null;
		}

		// ---- outlines around HUD boxes ----
		for (Module m : all) {
			if (m instanceof HudModule h && h.enabled) {
				Gfx.outline(ctx, h.clampedX(mc) - 1, h.clampedY(mc) - 1, h.width(mc) + 2, h.height(mc) + 2,
						h == dragging ? Gfx.WHITE : Gfx.ACCENT);
			}
		}

		// ---- panel ----
		ctx.fill(px, py, px + panelW(), py + panelH(), 0xE0101010);
		ctx.fill(px, py, px + panelW(), py + 2, Gfx.ACCENT);
		Gfx.text(ctx, mc.textRenderer, "Cat Client", px + 8, py + 8, Gfx.ACCENT);
		Gfx.text(ctx, mc.textRenderer, "click = toggle  |  right-click = colour  |  drag HUD boxes", px + 8, py + 20, Gfx.GRAY);

		Module hover = inPanel ? moduleAt(mx, my) : null;
		int rowsPerCol = (all.size() + COLS - 1) / COLS;
		for (int i = 0; i < all.size(); i++) {
			Module m = all.get(i);
			int col = i / rowsPerCol;
			int row = i % rowsPerCol;
			int rx = px + 6 + col * COL_W;
			int ry = py + 36 + row * ROW_H;
			ctx.fill(rx, ry, rx + COL_W - 4, ry + ROW_H - 2, m == hover ? 0xFF2A2A2A : 0xFF1A1A1A);
			ctx.fill(rx, ry, rx + 2, ry + ROW_H - 2, m.enabled ? Gfx.GREEN : Gfx.RED);
			Gfx.text(ctx, mc.textRenderer, m.name, rx + 6, ry + 3, Gfx.WHITE);
			String st = m.enabled ? "ON" : "OFF";
			int stX = rx + COL_W - 8 - mc.textRenderer.getWidth(st);
			Gfx.text(ctx, mc.textRenderer, st, stX, ry + 3, m.enabled ? Gfx.GREEN : Gfx.RED);
			if (m.usesColor) {
				ctx.fill(stX - 10, ry + 4, stX - 4, ry + 10, 0xFF000000 | m.color);
			}
		}
		String desc = hover != null ? hover.description : "Esc to close - settings are saved automatically";
		Gfx.text(ctx, mc.textRenderer, desc, px + 8, py + panelH() - 12, Gfx.GRAY);
	}

	private boolean hits(MinecraftClient mc, HudModule h, int mx, int my) {
		int x = h.clampedX(mc);
		int y = h.clampedY(mc);
		return mx >= x && mx < x + h.width(mc) && my >= y && my < y + h.height(mc);
	}

	private Module moduleAt(int mx, int my) {
		int px = panelX();
		int py = panelY();
		List<Module> all = Modules.ALL;
		int rowsPerCol = (all.size() + COLS - 1) / COLS;
		for (int i = 0; i < all.size(); i++) {
			int col = i / rowsPerCol;
			int row = i % rowsPerCol;
			int rx = px + 6 + col * COL_W;
			int ry = py + 36 + row * ROW_H;
			if (mx >= rx && mx < rx + COL_W - 4 && my >= ry && my < ry + ROW_H - 2) {
				return all.get(i);
			}
		}
		return null;
	}
}
