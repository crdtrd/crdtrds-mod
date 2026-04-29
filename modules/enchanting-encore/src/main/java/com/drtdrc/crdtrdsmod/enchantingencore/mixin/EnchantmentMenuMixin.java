package com.drtdrc.crdtrdsmod.enchantingencore.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import com.drtdrc.crdtrdsmod.enchantingencore.BiasContext;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Shadow
    @Final
    private ContainerLevelAccess access;

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void crdtrdsmod$prepareBias(Container container, CallbackInfo ci) {
        if (!ModConfig.active().enchantingEncore) return;
        access.execute(BiasContext::compute);
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    private void crdtrdsmod$clearBias(Container container, CallbackInfo ci) {
        if (!ModConfig.active().enchantingEncore) return;
        BiasContext.deactivate();
    }

    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void crdtrdsmod$prepareBiasOnApply(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.active().enchantingEncore) return;
        access.execute(BiasContext::compute);
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void crdtrdsmod$clearBiasOnApply(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.active().enchantingEncore) return;
        BiasContext.deactivate();
    }
}
