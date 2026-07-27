package com.drtdrc.crdtrdsmod.phantomnerf.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Nerfs natural phantom spawning: caps the pack size at 2 on every difficulty and lets each player
 * trigger a phantom spawn at most once per night, only after midnight.
 */
@Mixin(PhantomSpawner.class)
public class PhantomSpawnerMixin {

    @Unique
    private static final long DAY_LENGTH_TICKS = 24000L;
    /** Time of day (in ticks) at which midnight falls; phantoms may only spawn from here on. */
    @Unique
    private static final long MIDNIGHT_TICKS = 18000L;

    // Per-player night (clock day number) on which a phantom spawn was last allowed for that player.
    @Unique
    private final Map<UUID, Long> crdtrdsmod$lastSpawnNight = new HashMap<>();

    /**
     * Vanilla derives the pack size from {@code 1 + random.nextInt(difficultyId + 1)}. Clamping the
     * difficulty id to 1 caps the maximum pack size at 2 regardless of difficulty.
     */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Difficulty;getId()I")
    )
    private int crdtrdsmod$capPackSize(final int difficultyId) {
        return Math.min(difficultyId, 1);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;isValidEmptySpawnBlock(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/entity/EntityType;)Z")
    )
    private boolean crdtrdsmod$gateSpawn(final BlockGetter blockGetter, final BlockPos pos,
                                         final BlockState blockState, final FluidState fluidState,
                                         final EntityType<?> type,
                                         @Local(argsOnly = true) final ServerLevel level,
                                         @Local final ServerPlayer player) {
        if (!NaturalSpawner.isValidEmptySpawnBlock(blockGetter, pos, blockState, fluidState, type)) {
            return false;
        }
        long clock = level.getDefaultClockTime();
        long night = Math.floorDiv(clock, DAY_LENGTH_TICKS);
        long timeOfDay = Math.floorMod(clock, DAY_LENGTH_TICKS);
        if (timeOfDay < MIDNIGHT_TICKS) {
            return false;
        }
        UUID id = player.getUUID();
        Long last = crdtrdsmod$lastSpawnNight.get(id);
        if (last != null && last == night) {
            return false;
        }
        crdtrdsmod$lastSpawnNight.put(id, night);
        return true;
    }
}
