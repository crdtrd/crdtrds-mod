package com.drtdrc.crdtrdsmod.spawneggdrops;

import com.drtdrc.crdtrdsmod.ModConfig;
import com.drtdrc.crdtrdsmod.eggdrops.SpawnEggDrops;
import net.fabricmc.api.ModInitializer;

public class SpawneggdropsInit implements ModInitializer {
    @Override
    public void onInitialize() {
        if (ModConfig.active().spawnEggDrops) {
            SpawnEggDrops.register();
        }
    }
}
