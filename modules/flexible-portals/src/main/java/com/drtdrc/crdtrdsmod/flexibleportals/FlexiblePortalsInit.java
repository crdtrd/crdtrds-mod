package com.drtdrc.crdtrdsmod.flexibleportals;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;

public class FlexiblePortalsInit implements ModInitializer {

    @Override
    public void onInitialize() {
        if (ModConfig.active().flexiblePortals) {
            ModContainer container = FabricLoader.getInstance().getModContainer("crdtrdsmod_flexible_portals").orElseThrow();
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath("crdtrdsmod_flexible_portals", "flexible_portals_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
    }
}
