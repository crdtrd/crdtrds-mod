package com.drtdrc.crdtrdsmod.mixin.sleep;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.TickRateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelSleepMixin {

    @Unique
    private static final float SLEEP_TICK_RATE = 200.0f;
    @Unique
    private static final float NORMAL_TICK_RATE = 20.0f;
    @Unique
    private boolean crdtrdsmod$wasSleeping = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void crdtrdsmod$tickWarpSleep(BooleanSupplier haveTime, CallbackInfo ci) {
        if (!ModConfig.get().tickWarpSleep) return;
        ServerLevel self = (ServerLevel) (Object) this;
        TickRateManager trm = self.tickRateManager();

        boolean anySleeping = self.players().stream().anyMatch(p -> p.isSleeping());
        if (anySleeping && !crdtrdsmod$wasSleeping) {
            trm.setTickRate(SLEEP_TICK_RATE);
            crdtrdsmod$wasSleeping = true;
        } else if (!anySleeping && crdtrdsmod$wasSleeping) {
            trm.setTickRate(NORMAL_TICK_RATE);
            crdtrdsmod$wasSleeping = false;
        }
    }
}
