package com.drtdrc.crdtrdsmod.mixin.sleep;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSleepMixin {

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void crdtrdsmod$onWakeUp(boolean forcefulWakeUp, boolean updateLevelList, CallbackInfo ci) {
        if (!ModConfig.active().tickWarpSleep) return;
        // Tick rate reset is handled by ServerLevelSleepMixin detecting no sleeping players
    }
}
