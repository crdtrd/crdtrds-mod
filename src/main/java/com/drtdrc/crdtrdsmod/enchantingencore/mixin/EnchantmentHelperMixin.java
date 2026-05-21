package com.drtdrc.crdtrdsmod.enchantingencore.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import com.drtdrc.crdtrdsmod.enchantingencore.EnchantmentSelectionBiasContext;
import com.google.common.collect.Lists;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import static net.minecraft.world.item.enchantment.EnchantmentHelper.getAvailableEnchantmentResults;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Shadow
    public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> enchants, Holder<Enchantment> target) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(
            method = "getEnchantmentCost",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void crdtrdsmod$modifyEnchantmentCost(RandomSource random, int slotIndex, int bookshelfCount, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) {
            cir.setReturnValue(0);
        }
        else if ("casual".equals(ModConfig.active().enchantingEncore)) {
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
        else {
            if (bookshelfCount > 500) {
                bookshelfCount = 500;
            }
            int i = random.nextInt(4) + 1 + (bookshelfCount >> 1);
            if (slotIndex == 0) {
                cir.setReturnValue(Math.max(i / 3, 1));
            } else {
                cir.setReturnValue(slotIndex == 1 ? i * 2 / 3 + 100 : bookshelfCount * 2);
            }
        }
    }

//    @ModifyArg(
//            method = "selectEnchantment",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/util/random/WeightedRandom;getRandomItem(Lnet/minecraft/util/RandomSource;Ljava/util/List;Ljava/util/function/ToIntFunction;)Ljava/util/Optional;"
//            ),
//            index = 2
//    )
//    private static ToIntFunction<EnchantmentInstance> crdtrdsmod$biasWeightFunction(
//            ToIntFunction<EnchantmentInstance> original
//    ) {
//        return entry -> {
//            int base = original.applyAsInt(entry);
//            int bonus = EnchantmentSelectionBiasContext.bonus(entry.enchantment());
//            return Math.max(1, base + bonus);
//        };
//    }

    @Inject(
            method = "selectEnchantment",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void crdtrdsmod$newSelectEnchantment(RandomSource random, ItemStack itemStack, int enchantmentCost, Stream<Holder<Enchantment>> source, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        List<EnchantmentInstance> results = Lists.<EnchantmentInstance>newArrayList();
        Enchantable enchantable = itemStack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) {
            cir.setReturnValue(results);
        } else {
            enchantmentCost += 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
            float randomSpan = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
            enchantmentCost = Mth.clamp(Math.round(enchantmentCost + enchantmentCost * randomSpan), 1, Integer.MAX_VALUE);
            List<EnchantmentInstance> enchantments = getAvailableEnchantmentResults(enchantmentCost, itemStack, source);
            if (!enchantments.isEmpty()) {

                ToIntFunction<EnchantmentInstance> weightFn = addChiseledBookshelfWeightBias(EnchantmentInstance::weight);
                int totalWeight = WeightedRandom.getTotalWeight(enchantments, weightFn);

                // if any enchantment has weight > 50% of total, it becomes the sole candidate
                enchantments = filterMajorityEnchantment(enchantments, weightFn, totalWeight);

                // select and add first enchantment
                WeightedRandom.getRandomItem(random, enchantments, weightFn).ifPresent(results::add);

                while (random.nextInt(50) <= enchantmentCost) {

                    // keep incompatible enchants in selection pool, only skip if not compatible
                    EnchantmentInstance additionalEnchantment = WeightedRandom.getRandomItem(random, enchantments, weightFn).orElseThrow();

                    boolean compatible = true;
                    for (EnchantmentInstance e : results) {
                        if (!Enchantment.areCompatible(e.enchantment(), additionalEnchantment.enchantment())) {
                            compatible = false;
                            break;
                        }
                    }
                    if (compatible) results.add(additionalEnchantment);

                    enchantmentCost /= 2;
                }
            }

            cir.setReturnValue(results);
        }
    }

    @Unique
    private static ToIntFunction<EnchantmentInstance> addChiseledBookshelfWeightBias(
            ToIntFunction<EnchantmentInstance> original
    ) {
        return entry -> {
            int base = original.applyAsInt(entry);
            int bonus = EnchantmentSelectionBiasContext.bonus(entry.enchantment());

            return Math.max(1, base + bonus);
        };
    }

    @Unique
    private static List<EnchantmentInstance> filterMajorityEnchantment(
            List<EnchantmentInstance> enchantments,
            ToIntFunction<EnchantmentInstance> weightFn,
            int totalWeight
    ) {
        for (EnchantmentInstance entry : enchantments) {
            if (weightFn.applyAsInt(entry) * 2 > totalWeight) {
                return List.of(entry);
            }
        }
        return enchantments;
    }

}
