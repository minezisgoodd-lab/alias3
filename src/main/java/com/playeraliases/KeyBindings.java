package com.playeraliases;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * Keybindings show up automatically under Options > Controls > PlayerAliases,
 * so they are configurable/rebindable by the user with no extra work.
 */
public final class KeyBindings {

    public static final KeyBinding OPEN_GUI = new KeyBinding(
            "key.playeraliases.opengui",
            Keyboard.KEY_O,
            "key.categories.playeraliases"
    );

    private KeyBindings() {
    }
}
