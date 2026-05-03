package com.drtdrc.crdtrdsmod.compostableflesh.mixin;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void crdtrdsmod$addRottenFlesh(CallbackInfo ci) {
        ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH, 0.3f);
    }
}
