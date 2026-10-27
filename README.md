# Cat Client (Fabric, Minecraft 1.21.11)

Client-side PvP HUD mod. Fabric API + Yarn mappings, Java 21.

## Build
1. Install JDK 21.
2. No Gradle wrapper is included. Easiest: open the folder in IntelliJ IDEA (it sets Gradle up), or copy the
   `gradlew`, `gradlew.bat` and `gradle/` folder from the Fabric example mod (github.com/FabricMC/fabric-example-mod).
   Then run `./gradlew build` -> `build/libs/catclient-1.0.0.jar`
3. Put the jar + Fabric API 1.21.11 into `.minecraft/mods` with Fabric Loader.
4. Dev run: `./gradlew runClient`

If Gradle complains about versions, take current ones from https://fabricmc.net/develop
and edit `gradle.properties` (loom_version, loader_version, fabric_api_version, yarn_mappings).
If Gradle itself is too old: `./gradlew wrapper --gradle-version latest`.

## Use
- Right Shift: menu. Click = toggle module, drag HUD boxes to move. Esc closes. Saved to `config/catclient.json`.
- C (hold): zoom. Rebind in Controls -> Cat Client.

## Modules (36)
HUD: FPS, CPS, Ping, Coordinates, Direction, Combo, Reach, Hit Counter, Clock, Memory, Keystrokes, Potion Effects,
Cooldowns, Armor Status, PvP Items, Target HUD, Combat Stats, Mace Helper, Crosshair, Low HP warning, Dynamic Island (notifications).
Visuals: ESP, Body Glow, See Invisible, Target ESP, Trails, Hit Particles, Kill Effects, Star Glow, World Particles,
Atmosphere, Hit Range, HitSounds.
Utility: Zoom, Toggle Sprint, Fullbright.
Right-click a module with a colour swatch in the menu to cycle its colour.

## Adding a module
Add one line to `module/Modules.java` (`TextModule` for a simple "Label: value" box) or subclass `HudModule` / `Module`.

## Notes
Fullbright is client-side night vision. Zoom changes FOV/sensitivity while C is held.
ESP/Body Glow use the vanilla glow outline; Target ESP, Trails, Hit hints etc. are vanilla particles.
Not included (need Mixins/render code or a backend): sky shaders, Motion Blur, Tracers, Prediction, CustomHand,
freelook, cosmetics, friends/chat, server helpers. ESP / See Invisible / Hit Range give an advantage and are
banned on many servers - use at your own risk.
Using it on servers is at your own risk; none of the modules change what you send to the server.
