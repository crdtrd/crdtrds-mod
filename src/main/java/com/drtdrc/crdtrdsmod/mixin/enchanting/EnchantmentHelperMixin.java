package com.drtdrc.crdtrdsmod.mixin.enchanting;

import com.drtdrc.crdtrdsmod.ModConfig;
import com.drtdrc.crdtrdsmod.enchanting.BiasContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.ToIntFunction;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(
            method = "getEnchantmentCost",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void crdtrdsmod$modifyEnchantmentCost(RandomSource random, int slotIndex, int bookshelfCount, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!ModConfig.get().enchantingEncore) return;
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) {
            cir.setReturnValue(0);
        } else {
            if (bookshelfCount > 50) {
                bookshelfCount = 50;
            }
            int i = random.nextInt(4) + 1 + (bookshelfCount >> 1);
            if (slotIndex == 0) {
                cir.setReturnValue(Math.max(i / 3, 1));
            } else {
                cir.setReturnValue(slotIndex == 1 ? i * 2 / 3 + 1 : bookshelfCount);
            }
        }
    }

    @ModifyArg(
            method = "selectEnchantment",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/random/WeightedRandom;getRandomItem(Lnet/minecraft/util/RandomSource;Ljava/util/List;Ljava/util/function/ToIntFunction;)Ljava/util/Optional;"
            ),
            index = 2
    )
    private static ToIntFunction<EnchantmentInstance> crdtrdsmod$biasWeightFunction(
            ToIntFunction<EnchantmentInstance> original
    ) {
        if (!ModConfig.get().enchantingEncore) return original;
        return entry -> {
            int base = original.applyAsInt(entry);
            int bonus = BiasContext.bonus(entry.enchantment());
            return Math.max(1, base + bonus);
        };
    }
}
