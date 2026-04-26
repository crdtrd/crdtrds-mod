package com.drtdrc.crdtrdsmod.mixin.enchanting;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static final Identifier WATER_DS_BOOST_ID = Identifier.fromNamespaceAndPath("crdtrdsmod", "water_ds_boost");

    @Inject(method = "travel", at = @At("HEAD"))
    private void crdtrdsmod$applyWaterSpeedBoost(Vec3 input, CallbackInfo ci) {
        if (!ModConfig.get().enchantingEncore) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;

        var inst = self.getAttribute(Attributes.MOVEMENT_SPEED);
        if (inst == null) return;

        inst.removeModifier(WATER_DS_BOOST_ID);

        if (self.isInWater()) {
            double hRaw = self.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
            double bonus = Math.max(0.0, hRaw - 1.0);
            if (bonus > 0.0) {
                double k = 0.70;
                inst.addTransientModifier(new AttributeModifier(
                        WATER_DS_BOOST_ID,
                        k * bonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }
    }
}
