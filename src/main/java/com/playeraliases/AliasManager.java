package com.playeraliases;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds all known aliases in memory and persists them to
 * config/playeraliases/aliases.json using Gson.
 */
public final class AliasManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<UUID, PlayerAlias> ALIASES = new HashMap<>();
    private static File saveFile;

    private AliasManager() {
    }

    public static void init() {
        File configDir = new File(Loader.instance().getConfigDir(), "playeraliases");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        saveFile = new File(configDir, "aliases.json");
        load();
    }

    public static void load() {
        ALIASES.clear();
        if (saveFile == null || !saveFile.exists()) {
            return;
        }
        try (Reader reader = new FileReader(saveFile)) {
            Type type = new TypeToken<Map<String, PlayerAlias>>() {
            }.getType();
            Map<String, PlayerAlias> raw = GSON.fromJson(reader, type);
            if (raw != null) {
                for (Map.Entry<String, PlayerAlias> entry : raw.entrySet()) {
                    try {
                        ALIASES.put(UUID.fromString(entry.getKey()), entry.getValue());
                    } catch (IllegalArgumentException ignored) {
                        // Skip malformed UUID keys rather than crash the whole load.
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        if (saveFile == null) {
            return;
        }
        try (Writer writer = new FileWriter(saveFile)) {
            Map<String, PlayerAlias> raw = new HashMap<>();
            for (Map.Entry<UUID, PlayerAlias> entry : ALIASES.entrySet()) {
                raw.put(entry.getKey().toString(), entry.getValue());
            }
            GSON.toJson(raw, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAlias(UUID uuid, String originalName, String alias) {
        ALIASES.put(uuid, new PlayerAlias(uuid.toString(), originalName, alias, true));
        save();
    }

    public static void removeAlias(UUID uuid) {
        if (ALIASES.remove(uuid) != null) {
            save();
        }
    }

    public static PlayerAlias getAlias(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return ALIASES.get(uuid);
    }

    public static Map<UUID, PlayerAlias> getAll() {
        return ALIASES;
    }
}
