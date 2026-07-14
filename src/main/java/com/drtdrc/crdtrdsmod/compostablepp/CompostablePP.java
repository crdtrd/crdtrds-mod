package com.drtdrc.crdtrdsmod.compostablepp;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;

public class CompostablePP {
    public static void init() {
        ComposterBlock.COMPOSTABLES.put(Items.POISONOUS_POTATO, 0.3f);
    }
}
