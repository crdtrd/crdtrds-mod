package com.drtdrc.crdtrdsmod.spawneggdrops;

import net.fabricmc.api.ModInitializer;

public class SpawnEggDropsInit implements ModInitializer {
    @Override
    public void onInitialize() {
        SpawnEggDrops.register();
    }
}
