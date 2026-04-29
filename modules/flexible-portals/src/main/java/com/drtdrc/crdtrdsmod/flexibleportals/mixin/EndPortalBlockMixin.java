package com.drtdrc.crdtrdsmod.flexibleportals.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(method = "canBeReplaced", at = @At("HEAD"), cancellable = true)
    void crdtrdsmod$onCanBeReplaced(BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (ModConfig.active().flexiblePortals) {
            cir.setReturnValue(true);
        }
    }
}
