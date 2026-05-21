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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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

                // if chiseled bookshelves are present, restrict to only their enchantments
                if (EnchantmentSelectionBiasContext.hasChiseledBookshelves()) {
                    enchantments = filterToChiseledBookshelfEnchantments(enchantments);
                }

                if (!enchantments.isEmpty()) {
                    // weight function: use bookshelf power levels when present, otherwise base weight
                    ToIntFunction<EnchantmentInstance> weightFn = chiseledBookshelfWeightFn();

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
            }

            cir.setReturnValue(results);
        }
    }

    @Unique
    private static List<EnchantmentInstance> filterToChiseledBookshelfEnchantments(
            List<EnchantmentInstance> enchantments
    ) {
        Set<Holder<Enchantment>> allowed = EnchantmentSelectionBiasContext.allowedEnchantments();
        List<EnchantmentInstance> filtered = new ArrayList<>();
        for (EnchantmentInstance entry : enchantments) {
            if (allowed.contains(entry.enchantment())) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    @Unique
    private static ToIntFunction<EnchantmentInstance> chiseledBookshelfWeightFn() {
        if (EnchantmentSelectionBiasContext.hasChiseledBookshelves()) {
            return entry -> Math.max(1, EnchantmentSelectionBiasContext.weight(entry.enchantment()));
        }
        return EnchantmentInstance::weight;
    }

}
