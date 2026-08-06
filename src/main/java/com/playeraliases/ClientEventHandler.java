package com.playeraliases;

import com.playeraliases.gui.GuiAliasManager;
import com.playeraliases.gui.GuiQuickEdit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Mouse;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class ClientEventHandler {

    private static boolean prevMiddleDown = false;

    private ClientEventHandler() {
    }

    /**
     * Fired by Forge from EntityPlayer#getDisplayName(). This backs both the
     * nametag rendered above a player (RenderLivingBase uses getDisplayName)
     * and any other vanilla usage that reads the player's display name.
     */
    @SubscribeEvent
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        UUID id = event.getEntityPlayer().getGameProfile().getId();
        PlayerAlias alias = AliasManager.getAlias(id);
        if (alias != null && alias.enabled && alias.alias != null && !alias.alias.isEmpty()) {
            event.setDisplayname(alias.getFormattedAlias());
        }
    }

    /**
     * Replaces the sender's username inside incoming chat messages.
     * Vanilla chat lines already arrive as fully-built text components from
     * the server, so we do a text-level replace of the raw username with the
     * colored alias rather than trying to rebuild the component tree.
     */
    @SubscribeEvent
    public static void onChat(ClientChatReceivedEvent event) {
        Map<UUID, PlayerAlias> all = AliasManager.getAll();
        if (all.isEmpty()) {
            return;
        }

        String text = event.getMessage().getFormattedText();
        boolean changed = false;

        for (PlayerAlias pa : all.values()) {
            if (!pa.enabled || pa.alias == null || pa.alias.isEmpty()) {
                continue;
            }
            if (pa.originalName != null && !pa.originalName.isEmpty() && text.contains(pa.originalName)) {
                text = text.replace(pa.originalName, pa.getFormattedAlias() + "\u00a7r");
                changed = true;
            }
        }

        if (changed) {
            event.setMessage(new TextComponentString(text));
        }
    }

    /**
     * Applies aliases to the tab (player list) overlay just before it renders.
     * NetworkPlayerInfo#setDisplayName is normally driven by the server, so we
     * re-apply our override every frame the overlay is visible to make sure it
     * sticks even if the server refreshes player info.
     */
    @SubscribeEvent
    public static void onRenderTabList(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getConnection() == null) {
            return;
        }
        for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
            PlayerAlias pa = AliasManager.getAlias(info.getGameProfile().getId());
            if (pa != null && pa.enabled && pa.alias != null && !pa.alias.isEmpty()) {
                info.setDisplayName(new TextComponentString(pa.getFormattedAlias()));
            }
        }
    }

    /**
     * Handles the "open GUI" keybind and raw middle-click-on-player detection.
     * Middle click is polled directly via LWJGL rather than a Forge input
     * event so it works reliably regardless of Forge's mouse event variant.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            prevMiddleDown = false;
            return;
        }

        while (KeyBindings.OPEN_GUI.isPressed()) {
            if (mc.currentScreen == null) {
                mc.displayGuiScreen(new GuiAliasManager());
            }
        }

        boolean middleDown = Mouse.isButtonDown(2);
        if (middleDown && !prevMiddleDown && mc.currentScreen == null) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
                EntityPlayer target = (EntityPlayer) mc.objectMouseOver.entityHit;
                if (target != mc.player) {
                    mc.displayGuiScreen(new GuiQuickEdit(
                            target.getGameProfile().getId(),
                            target.getGameProfile().getName()
                    ));
                }
            }
        }
        prevMiddleDown = middleDown;
    }
}
