package com.drtdrc.crdtrdsmod.enchantingencore;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;

public class EnchantingEncoreInit implements ModInitializer {

    @Override
    public void onInitialize() {
        if (ModConfig.active().enchantingEncore) {
            ModContainer container = FabricLoader.getInstance().getModContainer("crdtrdsmod_enchanting_encore").orElseThrow();
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath("crdtrdsmod_enchanting_encore", "enchantingencore_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
    }
}
