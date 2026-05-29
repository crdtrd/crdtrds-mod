package com.drtdrc.crdtrdsmod.mixin.client;

import com.drtdrc.crdtrdsmod.goafk.FakePlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FakePlayerPickMixin {

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void goafk_disableFakePlayerPick(CallbackInfoReturnable<Boolean> cir) {
        if (FakePlayer.isFake((Player) (Object) this)) {
            cir.setReturnValue(false);
        }
    }
}
