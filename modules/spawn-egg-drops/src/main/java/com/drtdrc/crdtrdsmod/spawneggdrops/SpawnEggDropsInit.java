package com.drtdrc.crdtrdsmod.spawneggdrops;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;

public class SpawnEggDropsInit implements ModInitializer {
    @Override
    public void onInitialize() {
        if (ModConfig.active().spawnEggDrops) {
            SpawnEggDrops.register();
        }
    }
}
