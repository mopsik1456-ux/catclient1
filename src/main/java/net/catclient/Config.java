package net.catclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.catclient.module.HudModule;
import net.catclient.module.Module;
import net.catclient.module.Modules;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Saves module on/off state and HUD positions to config/catclient.json. */
public final class Config {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("catclient.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static class Entry {
		boolean enabled;
		int x;
		int y;
		int color;
	}

	private Config() {
	}

	public static void load() {
		try {
			if (!Files.exists(FILE)) {
				return;
			}
			Type type = new TypeToken<Map<String, Entry>>() {
			}.getType();
			Map<String, Entry> map = GSON.fromJson(Files.readString(FILE), type);
			if (map == null) {
				return;
			}
			for (Module m : Modules.ALL) {
				Entry e = map.get(m.name);
				if (e == null) {
					continue;
				}
				m.enabled = e.enabled;
				if (e.color != 0) {
					m.color = e.color;
				}
				if (m instanceof HudModule h) {
					h.x = e.x;
					h.y = e.y;
				}
			}
		} catch (Exception e) {
			System.err.println("[CatClient] Could not read config: " + e);
		}
	}

	public static void save() {
		try {
			Map<String, Entry> map = new LinkedHashMap<>();
			for (Module m : Modules.ALL) {
				Entry e = new Entry();
				e.enabled = m.enabled;
				e.color = m.color;
				if (m instanceof HudModule h) {
					e.x = h.x;
					e.y = h.y;
				}
				map.put(m.name, e);
			}
			Files.writeString(FILE, GSON.toJson(map));
		} catch (Exception e) {
			System.err.println("[CatClient] Could not save config: " + e);
		}
	}
}
