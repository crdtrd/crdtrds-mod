package com.drtdrc.crdtrdsmod.undeadvariantbuff.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.entity.monster.Monster.checkMonsterSpawnRules;

@Mixin(Monster.class)
public class MonsterMixin {

    @Inject(
            method = "checkSurfaceMonstersSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void crdtrdsmod$checkSurfaceMonstersSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {

        if (type == EntityType.HUSK ||  type == EntityType.PARCHED) {

            cir.setReturnValue(checkMonsterSpawnRules(type, level, spawnReason, pos, random) || EntitySpawnReason.isSpawner(spawnReason));
        }
    }
}
