# PlayerAliases (Forge 1.12.2, client-side only)

Locally rename any player you see in-game. Nothing is sent to the server or
other players — this only changes what *you* see, in nametags, the tab list,
and chat.

## Features implemented

- Rename any player locally, stored per-UUID
- `&` color codes (e.g. `&c&lBob`) converted to real formatting
- Nametag replacement (via `PlayerEvent.NameFormat`)
- Tab list replacement (via `NetworkPlayerInfo#setDisplayName`, reapplied every frame)
- Chat name replacement (via `ClientChatReceivedEvent`, text-level replace)
- Searchable GUI (`GuiAliasManager`) listing online players + saved aliases, with a search box
- Middle-click a player to quick-edit their alias (`GuiQuickEdit`)
- JSON save file at `config/playeraliases/aliases.json` (via Gson)
- Configurable keybind (Options > Controls > PlayerAliases > "Open Alias Manager", default `O`)

## ⚠️ Important: this sandbox could not compile the JAR

This code was written and reviewed by hand against known Forge/MCP 1.12.2
signatures, but it has **not** been run through an actual Gradle build here —
the environment that generated this project has no internet access, and
ForgeGradle's first build needs to download Minecraft, Forge, and MCP
mappings (hundreds of MB). You will need to build it yourself. Instructions
below, plus a "if it doesn't compile" checklist of the specific spots most
likely to need a one-line fix.

## How to build

Requirements: JDK 8 (ForgeGradle 2.3 does **not** work with JDK 9+), internet
access for the first build.

```bash
cd PlayerAliases
# Linux/macOS
chmod +x gradlew   # if gradlew script/binary is present; otherwise use `gradle` directly
./gradlew build

# Windows
gradlew.bat build
```

If you don't have a `gradlew` wrapper jar (this project ships the
`gradle-wrapper.properties` config but not the binary `gradle-wrapper.jar`,
since generating it requires network access too), either:

1. Run `gradle wrapper` once with a locally installed Gradle 4.x to generate it, then use `./gradlew build`, or
2. Just use a locally installed Gradle 4.9 directly: `gradle build`.

The first build will take a while (downloading + decompiling Minecraft).
Subsequent builds are fast.

The compiled JAR will be at `build/libs/playeraliases-1.0.0.jar`. Drop it into
your `.minecraft/mods` folder for Forge 1.12.2.

To test in a dev environment instead: `./gradlew runClient`.

## If the build fails: places to check first

1. **`minecraft.version` in `build.gradle`** — set to `1.12.2-14.23.5.2860`.
   If that exact Forge build is unavailable, swap in any current 1.12.2 recommended/latest build from
   https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html
2. **`PlayerEvent.NameFormat`** — used in `ClientEventHandler#onNameFormat`.
   The method names `getEntityPlayer()`, `setDisplayname(String)` are correct
   for 1.12.2, but if your exact Forge build differs, check
   `net.minecraftforge.event.entity.player.PlayerEvent.NameFormat` in the
   decompiled sources (`build/tmp/recompSrc` or via your IDE's "Go to
   Definition") and adjust the setter call if needed.
3. **`RenderGameOverlayEvent.ElementType.PLAYER_LIST`** — used for the tab
   list hook. This enum constant has been stable across 1.12.x, but double
   check it exists in `net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType`.
4. **JDK version** — ForgeGradle 2.3 requires Java 8. If you have a newer JDK
   as default, install JDK 8 and either set `JAVA_HOME` to it or configure
   `org.gradle.java.home` in `gradle.properties`.
5. Gson is bundled with Minecraft already, so no extra dependency was added —
   if you see `ClassNotFoundException: com.google.gson...` at runtime, your
   Forge version is unusually stripped down; add `compile 'com.google.code.gson:gson:2.8.0'` under `dependencies {}` in `build.gradle`.

## Known limitations (by design of a config-free client mod)

- Middle-click detection is done by polling the mouse button state every
  client tick rather than hooking a Forge mouse-input event, to avoid pinning
  to a specific event class signature. This works reliably but does not
  cancel vanilla's own middle-click "pick" behavior — in survival that's a
  no-op on players anyway, so it shouldn't cause visible conflicts.
- Chat replacement is a text-level substring replace of the original
  username inside the formatted chat line, not a full text-component
  rewrite. This covers the standard `<Name> message` format and most
  addon chat formats, but a heavily custom server-side chat format that
  doesn't include the plain username string won't be caught.
- If two different players you're aliasing happen to share a substring in
  their names, alias substitution is applied longest-registered-name-first
  isn't guaranteed — for typical usernames this isn't an issue, but very
  short/overlapping names could interact. Let me know if you want this
  hardened with word-boundary matching.
