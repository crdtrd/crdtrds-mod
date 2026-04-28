package com.drtdrc.crdtrdsmod.mixin.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class PortalServerLevelMixin {

    @Inject(method = "globalLevelEvent", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$endPortalOpenedLocal(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId == 1038) {
            ServerLevel self = (ServerLevel) (Object) this;
            self.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
            ci.cancel();
        }
    }
}
