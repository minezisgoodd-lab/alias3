package com.playeraliases;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = PlayerAliasesMod.MODID,
        name = PlayerAliasesMod.NAME,
        version = PlayerAliasesMod.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.12.2]"
)
public class PlayerAliasesMod {

    public static final String MODID = "playeraliases";
    public static final String NAME = "PlayerAliases";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        AliasManager.init();
        ClientRegistry.registerKeyBinding(KeyBindings.OPEN_GUI);
        // ClientEventHandler is registered automatically via @Mod.EventBusSubscriber.
    }
}
