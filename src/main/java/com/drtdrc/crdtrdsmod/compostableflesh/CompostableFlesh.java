package com.drtdrc.crdtrdsmod.compostableflesh;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;

public class CompostableFlesh {
    public static void init() {
        ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH, 0.3f);
    }
}
