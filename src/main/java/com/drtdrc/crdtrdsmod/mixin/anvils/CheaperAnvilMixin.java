package com.drtdrc.crdtrdsmod.mixin.anvils;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class CheaperAnvilMixin {

    @Inject(method = "calculateIncreasedRepairCost", at = @At("HEAD"), cancellable = true)
    private static void crdtrdsmod$cheaperRepairCost(int baseCost, CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.get().cheaperAnvils) return;
        cir.setReturnValue(baseCost + 2);
    }
}
