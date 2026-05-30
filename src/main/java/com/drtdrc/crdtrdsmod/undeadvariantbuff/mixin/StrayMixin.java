package com.drtdrc.crdtrdsmod.undeadvariantbuff.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Stray.class)
public class StrayMixin {

    @Inject(
            method = "checkStraySpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private static void crdtrdsmod$checkStraySpawnRules(EntityType<Stray> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(Monster.checkMonsterSpawnRules(type, level, spawnReason, pos, random)
                || EntitySpawnReason.isSpawner(spawnReason));
    }
}
