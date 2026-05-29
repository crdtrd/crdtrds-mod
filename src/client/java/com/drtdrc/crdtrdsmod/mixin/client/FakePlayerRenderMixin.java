package com.drtdrc.crdtrdsmod.mixin.client;

import com.drtdrc.crdtrdsmod.goafk.FakePlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class FakePlayerRenderMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void goafk_forceFakePlayerTranslucent(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (entity instanceof Player player && FakePlayer.isFake(player)) {
            state.isInvisibleToPlayer = false;
        }
    }
}
