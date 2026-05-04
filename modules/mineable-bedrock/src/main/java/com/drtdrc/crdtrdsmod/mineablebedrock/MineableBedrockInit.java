package com.drtdrc.crdtrdsmod.mineablebedrock;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;

public class MineableBedrockInit implements ModInitializer {

    @Override
    public void onInitialize() {
        if (ModConfig.active().mineableBedrock) {
            ModContainer container = FabricLoader.getInstance().getModContainer("crdtrdsmod_mineable_bedrock").orElseThrow();
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath("crdtrdsmod_mineable_bedrock", "mineable_bedrock_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
    }
}
