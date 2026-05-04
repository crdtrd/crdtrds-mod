package com.drtdrc.crdtrdsmod.cursestone.mixin;

import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public class CurseStoneMixin {

    @Inject(method = "removeNonCursesFrom", at = @At("RETURN"))
    private void removeCurses(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        EnchantmentHelper.updateEnchantments(cir.getReturnValue(), mutable -> {
            mutable.removeIf(holder -> holder.is(EnchantmentTags.CURSE));
        });
    }
}
