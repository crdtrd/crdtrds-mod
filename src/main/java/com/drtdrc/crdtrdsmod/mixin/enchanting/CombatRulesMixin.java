package com.drtdrc.crdtrdsmod.mixin.enchanting;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CombatRules.class)
public abstract class CombatRulesMixin {

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("HEAD"), cancellable = true)
    private static void crdtrdsmod$modifyMagicAbsorb(float damage, float protection, CallbackInfoReturnable<Float> cir) {
        if (!ModConfig.get().enchantingEncore) return;
        float f = Mth.clamp(protection, 0.0F, 35.0F);
        cir.setReturnValue(damage * (1.0F - f / 35.35F));
    }
}
