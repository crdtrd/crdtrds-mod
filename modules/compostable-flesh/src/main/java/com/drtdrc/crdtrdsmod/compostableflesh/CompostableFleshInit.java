package com.drtdrc.crdtrdsmod.compostableflesh;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;

public class CompostableFleshInit implements ModInitializer {
    @Override
    public void onInitialize() {
        if (ModConfig.active().compostableFlesh) {
            ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH, 0.3f);
        }
    }
}
