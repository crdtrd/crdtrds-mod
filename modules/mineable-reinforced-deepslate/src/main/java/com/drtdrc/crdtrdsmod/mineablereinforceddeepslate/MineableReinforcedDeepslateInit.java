package com.drtdrc.crdtrdsmod.mineablereinforceddeepslate;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;

public class MineableReinforcedDeepslateInit implements ModInitializer {

    @Override
    public void onInitialize() {
        if (ModConfig.active().mineableReinforcedDeepslate) {
            ModContainer container = FabricLoader.getInstance().getModContainer("crdtrdsmod_mineable_reinforced_deepslate").orElseThrow();
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath("crdtrdsmod_mineable_reinforced_deepslate", "mineable_reinforced_deepslate_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
    }
}
