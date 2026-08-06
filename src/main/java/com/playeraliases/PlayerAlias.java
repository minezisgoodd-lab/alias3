package com.playeraliases;

/**
 * Simple serializable data holder for one alias entry.
 * Kept as plain public fields so Gson can (de)serialize it with zero configuration.
 */
public class PlayerAlias {

    public String uuid;
    public String originalName;
    public String alias;
    public boolean enabled;

    // Required no-arg constructor for Gson.
    public PlayerAlias() {
    }

    public PlayerAlias(String uuid, String originalName, String alias, boolean enabled) {
        this.uuid = uuid;
        this.originalName = originalName;
        this.alias = alias;
        this.enabled = enabled;
    }

    /**
     * Converts '&' color codes typed by the user (e.g. "&c&lBob") into
     * real Minecraft section-sign color codes used for rendering.
     */
    public String getFormattedAlias() {
        if (alias == null) {
            return "";
        }
        return alias.replace('&', '\u00a7');
    }
}
