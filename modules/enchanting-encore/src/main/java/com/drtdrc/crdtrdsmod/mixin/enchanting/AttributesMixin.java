package com.drtdrc.crdtrdsmod.mixin.enchanting;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Attributes.class)
public class AttributesMixin {

    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/Attributes;register(Ljava/lang/String;Lnet/minecraft/world/entity/ai/attributes/Attribute;)Lnet/minecraft/core/Holder;"
            ),
            index = 1,
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=water_movement_efficiency")
            )
    )
    private static Attribute crdtrdsmod$replaceWaterMovementAttr(Attribute original) {
        if (!ModConfig.get().enchantingEncore) return original;
        return new RangedAttribute(
                "attribute.name.water_movement_efficiency",
                0.0, 0.0, 2.0
        ).setSyncable(true);
    }
}
