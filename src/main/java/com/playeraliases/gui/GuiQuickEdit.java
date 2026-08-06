package com.playeraliases.gui;

import com.playeraliases.AliasManager;
import com.playeraliases.PlayerAlias;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.UUID;

/**
 * Small popup for quickly setting/clearing an alias, opened by middle-clicking
 * a player while no other GUI is open.
 */
public class GuiQuickEdit extends GuiScreen {

    private static final int BTN_SAVE = 1;
    private static final int BTN_CLEAR = 2;

    private final UUID uuid;
    private final String originalName;
    private GuiTextField aliasField;

    public GuiQuickEdit(UUID uuid, String originalName) {
        this.uuid = uuid;
        this.originalName = originalName;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int cx = this.width / 2;
        int cy = this.height / 2;

        aliasField = new GuiTextField(0, this.fontRenderer, cx - 100, cy - 10, 200, 20);
        aliasField.setMaxStringLength(64);
        PlayerAlias existing = AliasManager.getAlias(uuid);
        aliasField.setText(existing != null && existing.alias != null ? existing.alias : "");
        aliasField.setFocused(true);

        this.buttonList.add(new GuiButton(BTN_SAVE, cx - 100, cy + 20, 95, 20, "Save"));
        this.buttonList.add(new GuiButton(BTN_CLEAR, cx + 5, cy + 20, 95, 20, "Clear Alias"));
    }

    @Override
    public void updateScreen() {
        aliasField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Quick Edit: " + originalName, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, "Use & for color codes, e.g. &c&lName", this.width / 2, this.height / 2 - 12, 0x888888);
        aliasField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_SAVE) {
            save();
        } else if (button.id == BTN_CLEAR) {
            AliasManager.removeAlias(uuid);
            this.mc.displayGuiScreen(null);
        }
    }

    private void save() {
        String text = aliasField.getText();
        if (text != null && !text.trim().isEmpty()) {
            AliasManager.setAlias(uuid, originalName, text);
        }
        this.mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            save();
            return;
        }
        if (aliasField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        aliasField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
