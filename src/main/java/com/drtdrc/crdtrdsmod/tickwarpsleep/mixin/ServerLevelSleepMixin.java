package com.drtdrc.crdtrdsmod.tickwarpsleep.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelSleepMixin {

    @Unique
    private static final float SLEEP_TICK_RATE = 200.0f;
    @Unique
    private static final float NORMAL_TICK_RATE = 20.0f;
    @Unique
    private boolean crdtrdsmod$warping = false;

    @Shadow
    @Final
    private SleepStatus sleepStatus;

    @Shadow
    public abstract TickRateManager tickRateManager();

    @Shadow
    public abstract List<ServerPlayer> players();

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"
            )
    )
    private boolean crdtrdsmod$onSleepCheck(SleepStatus instance, int percentage) {

        boolean playerReq = instance.areEnoughSleeping(percentage)
                && instance.areEnoughDeepSleeping(percentage, this.players());
        boolean night = ((Level) (Object) this).isDarkOutside();

        if (playerReq && night && !crdtrdsmod$warping) {
            crdtrdsmod$warping = true;
            this.tickRateManager().setTickRate(SLEEP_TICK_RATE);
        }
        if (playerReq && !night && crdtrdsmod$warping) {
            crdtrdsmod$warping = false;
            crdtrdsmod$wakeUpAllPlayers();
            this.tickRateManager().setTickRate(NORMAL_TICK_RATE);
        }
        if (!playerReq && !night && crdtrdsmod$warping) {
            crdtrdsmod$warping = false;
            crdtrdsmod$wakeUpAllPlayers();
            this.tickRateManager().setTickRate(NORMAL_TICK_RATE);
        }
        if (!playerReq && night && crdtrdsmod$warping) {
            crdtrdsmod$warping = false;
            this.tickRateManager().setTickRate(NORMAL_TICK_RATE);
        }

        return false;
    }

    @Unique
    private void crdtrdsmod$wakeUpAllPlayers() {
        this.sleepStatus.removeAllSleepers();
        for (ServerPlayer player : List.copyOf(this.players())) {
            if (player.isSleeping()) {
                player.stopSleepInBed(false, true);
            }
        }
    }
}
