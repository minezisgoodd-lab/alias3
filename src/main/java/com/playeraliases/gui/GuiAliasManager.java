package com.playeraliases.gui;

import com.playeraliases.AliasManager;
import com.playeraliases.PlayerAlias;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Full alias manager: searchable list of online players and known aliases,
 * with fields to add/edit and buttons to save/delete.
 *
 * The list is implemented as a lightweight hand-rolled scroll list rather
 * than GuiListExtended, to keep the widget self-contained and avoid pinning
 * to a specific GuiListExtended constructor signature.
 */
public class GuiAliasManager extends GuiScreen {

    private static final int BTN_SAVE = 1;
    private static final int BTN_DELETE = 2;
    private static final int BTN_CLOSE = 3;
    private static final int ROW_HEIGHT = 14;

    private GuiTextField searchField;
    private GuiTextField nameField;
    private GuiTextField aliasField;

    private final List<RowEntry> rows = new ArrayList<>();
    private int scrollOffset = 0;
    private int listLeft, listRight, listTop, listBottom;
    private String selectedUuid = null;

    private static final class RowEntry {
        UUID uuid;
        String name;
        String alias;
        boolean hasAlias;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int cx = this.width / 2;

        searchField = new GuiTextField(0, this.fontRenderer, cx - 150, 22, 300, 18);
        searchField.setMaxStringLength(64);

        nameField = new GuiTextField(1, this.fontRenderer, cx - 150, this.height - 68, 145, 18);
        nameField.setMaxStringLength(32);

        aliasField = new GuiTextField(2, this.fontRenderer, cx + 5, this.height - 68, 145, 18);
        aliasField.setMaxStringLength(64);

        listLeft = cx - 150;
        listRight = cx + 150;
        listTop = 50;
        listBottom = this.height - 100;

        this.buttonList.add(new GuiButton(BTN_SAVE, cx - 150, this.height - 42, 90, 20, "Save"));
        this.buttonList.add(new GuiButton(BTN_DELETE, cx - 55, this.height - 42, 90, 20, "Delete"));
        this.buttonList.add(new GuiButton(BTN_CLOSE, cx + 40, this.height - 42, 110, 20, "Close"));

        rebuildRows();
    }

    private void rebuildRows() {
        rows.clear();
        String filter = searchField != null ? searchField.getText().toLowerCase() : "";
        List<UUID> seen = new ArrayList<>();

        if (this.mc.getConnection() != null) {
            for (NetworkPlayerInfo info : this.mc.getConnection().getPlayerInfoMap()) {
                UUID id = info.getGameProfile().getId();
                String name = info.getGameProfile().getName();
                if (name == null) {
                    continue;
                }
                if (!filter.isEmpty() && !name.toLowerCase().contains(filter)) {
                    continue;
                }
                RowEntry r = new RowEntry();
                r.uuid = id;
                r.name = name;
                PlayerAlias pa = AliasManager.getAlias(id);
                r.hasAlias = pa != null;
                r.alias = pa != null ? pa.alias : "";
                rows.add(r);
                seen.add(id);
            }
        }

        for (PlayerAlias pa : AliasManager.getAll().values()) {
            if (pa.uuid == null || pa.originalName == null) {
                continue;
            }
            UUID id;
            try {
                id = UUID.fromString(pa.uuid);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            if (seen.contains(id)) {
                continue;
            }
            if (!filter.isEmpty() && !pa.originalName.toLowerCase().contains(filter)) {
                continue;
            }
            RowEntry r = new RowEntry();
            r.uuid = id;
            r.name = pa.originalName;
            r.alias = pa.alias;
            r.hasAlias = true;
            rows.add(r);
        }
    }

    @Override
    public void updateScreen() {
        searchField.updateCursorCounter();
        nameField.updateCursorCounter();
        aliasField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "PlayerAliases", this.width / 2, 6, 0xFFFFFF);
        this.drawString(this.fontRenderer, "Search:", listLeft, 10, 0xAAAAAA);
        searchField.drawTextBox();

        drawList(mouseX, mouseY);

        this.drawString(this.fontRenderer, "Name:", listLeft, this.height - 80, 0xAAAAAA);
        this.drawString(this.fontRenderer, "Alias (use & for colors):", listLeft + 155, this.height - 80, 0xAAAAAA);
        nameField.drawTextBox();
        aliasField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawList(int mouseX, int mouseY) {
        drawRect(listLeft, listTop, listRight, listBottom, 0x66000000);
        int visibleRows = (listBottom - listTop) / ROW_HEIGHT;
        int maxScroll = Math.max(0, rows.size() - visibleRows);
        if (scrollOffset > maxScroll) {
            scrollOffset = maxScroll;
        }

        for (int i = 0; i < visibleRows && i + scrollOffset < rows.size(); i++) {
            RowEntry r = rows.get(i + scrollOffset);
            int y = listTop + i * ROW_HEIGHT;
            boolean hovered = mouseX >= listLeft && mouseX <= listRight && mouseY >= y && mouseY < y + ROW_HEIGHT;
            boolean selected = r.uuid.toString().equals(selectedUuid);
            if (selected) {
                drawRect(listLeft, y, listRight, y + ROW_HEIGHT, 0x8855AA55);
            } else if (hovered) {
                drawRect(listLeft, y, listRight, y + ROW_HEIGHT, 0x55FFFFFF);
            }
            String display = r.hasAlias
                    ? r.name + "  \u00a77->\u00a7r " + r.alias.replace('&', '\u00a7')
                    : r.name + "  \u00a78(no alias)";
            this.fontRenderer.drawStringWithShadow(display, listLeft + 4, y + 3, 0xFFFFFF);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        aliasField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseX >= listLeft && mouseX <= listRight && mouseY >= listTop && mouseY < listBottom) {
            int idx = (mouseY - listTop) / ROW_HEIGHT + scrollOffset;
            if (idx >= 0 && idx < rows.size()) {
                RowEntry r = rows.get(idx);
                selectedUuid = r.uuid.toString();
                nameField.setText(r.name);
                aliasField.setText(r.alias != null ? r.alias : "");
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            scrollOffset -= wheel > 0 ? 1 : -1;
            if (scrollOffset < 0) {
                scrollOffset = 0;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            rebuildRows();
            return;
        }
        if (nameField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        if (aliasField.textboxKeyTyped(typedChar, keyCode)) {
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BTN_SAVE) {
            saveCurrent();
        } else if (button.id == BTN_DELETE) {
            deleteCurrent();
        } else if (button.id == BTN_CLOSE) {
            this.mc.displayGuiScreen(null);
        }
    }

    private void saveCurrent() {
        String name = nameField.getText().trim();
        String alias = aliasField.getText();
        if (name.isEmpty() || alias == null || alias.trim().isEmpty()) {
            return;
        }

        UUID uuid = null;
        if (selectedUuid != null) {
            uuid = UUID.fromString(selectedUuid);
        } else if (this.mc.getConnection() != null) {
            for (NetworkPlayerInfo info : this.mc.getConnection().getPlayerInfoMap()) {
                if (name.equalsIgnoreCase(info.getGameProfile().getName())) {
                    uuid = info.getGameProfile().getId();
                    break;
                }
            }
        }
        if (uuid == null) {
            // Fall back to an offline-style UUID so aliases can be pre-created
            // for players who are not currently online.
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
        }

        AliasManager.setAlias(uuid, name, alias);
        selectedUuid = uuid.toString();
        rebuildRows();
    }

    private void deleteCurrent() {
        if (selectedUuid != null) {
            AliasManager.removeAlias(UUID.fromString(selectedUuid));
            selectedUuid = null;
            nameField.setText("");
            aliasField.setText("");
            rebuildRows();
        }
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
