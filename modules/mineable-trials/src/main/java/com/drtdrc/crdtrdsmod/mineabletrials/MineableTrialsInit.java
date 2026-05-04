package com.drtdrc.crdtrdsmod.mineabletrials;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;

public class MineableTrialsInit implements ModInitializer {

    @Override
    public void onInitialize() {
        if (ModConfig.active().mineableTrials) {
            ModContainer container = FabricLoader.getInstance().getModContainer("crdtrdsmod_mineable_trials").orElseThrow();
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath("crdtrdsmod_mineable_trials", "mineable_trials_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
    }
}
