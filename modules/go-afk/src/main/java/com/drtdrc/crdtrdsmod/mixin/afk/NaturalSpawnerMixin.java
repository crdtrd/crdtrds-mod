package com.drtdrc.crdtrdsmod.mixin.afk;

import com.drtdrc.crdtrdsmod.afk.AFKManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(NaturalSpawner.class)
public abstract class NaturalSpawnerMixin {

    @Inject(
            method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void goafk$onSpawnEntitiesInChunk(MobCategory category, ServerLevel level, ChunkAccess chunk, BlockPos pos, NaturalSpawner.SpawnPredicate checker, NaturalSpawner.AfterSpawnCallback runner, CallbackInfo ci) {
        List<BlockPos> anchors = AFKManager.getAnchorPositions(level);
        if (anchors.isEmpty()) return;

        StructureManager structureManager = level.structureManager();
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        int py = pos.getY();
        BlockState blockState = chunk.getBlockState(pos);
        if (blockState.isRedstoneConductor(chunk, pos)) {
            ci.cancel();
            return;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int totalSpawnedThisCall = 0;

        for (int attempt = 0; attempt < 3; attempt++) {
            int baseX = pos.getX();
            int baseZ = pos.getZ();
            MobSpawnSettings.SpawnerData spawnEntry = null;
            SpawnGroupData entityData = null;
            int triesForPack = Mth.ceil(level.getRandom().nextFloat() * 4.0F);
            int spawnedInPack = 0;

            for (int t = 0; t < triesForPack; t++) {
                baseX += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
                baseZ += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
                mutable.set(baseX, py, baseZ);

                final double px = baseX + 0.5;
                final double pz = baseZ + 0.5;

                Double nearestSq = goafk$getNearestPlayerOrAnchorSq(level, anchors, px, py, pz);
                if (nearestSq == null) {
                    continue;
                }

                if (NaturalSpawnerInvokers.goafk$isRightDistanceToPlayerAndSpawnPoint(level, chunk, mutable, nearestSq)) {
                    if (spawnEntry == null) {
                        Optional<MobSpawnSettings.SpawnerData> optional = NaturalSpawnerInvokers.goafk$getRandomSpawnMobAt(level, structureManager, chunkGenerator, category, level.getRandom(), mutable);
                        if (optional.isEmpty()) {
                            continue;
                        }
                        spawnEntry = optional.get();
                        triesForPack = spawnEntry.minCount() + level.getRandom().nextInt(1 + spawnEntry.maxCount() - spawnEntry.minCount());
                    }

                    if (NaturalSpawnerInvokers.goafk$isValidSpawnPostitionForType(level, category, structureManager, chunkGenerator, spawnEntry, mutable, nearestSq)
                            && checker.test(spawnEntry.type(), mutable, chunk)) {

                        Mob mob = NaturalSpawnerInvokers.goafk$getMobForSpawn(level, spawnEntry.type());
                        if (mob == null) {
                            ci.cancel();
                            return;
                        }

                        mob.snapTo(px, (double) py, pz, level.getRandom().nextFloat() * 360.0F, 0.0F);
                        if (NaturalSpawnerInvokers.goafk$isValidPositionForMob(level, mob, nearestSq)) {
                            entityData = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.NATURAL, entityData);
                            totalSpawnedThisCall++;
                            spawnedInPack++;
                            level.addFreshEntity(mob);
                            runner.run(mob, chunk);

                            if (totalSpawnedThisCall >= mob.getMaxSpawnClusterSize()) {
                                ci.cancel();
                                return;
                            }
                            if (mob.isMaxGroupSizeReached(spawnedInPack)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        ci.cancel();
    }

    @Unique
    private static Double goafk$getNearestPlayerOrAnchorSq(ServerLevel level, List<BlockPos> anchors, double x, double y, double z) {
        Player nearestPlayer = level.getNearestPlayer(x, y, z, -1.0, false);
        double bestSq = Double.POSITIVE_INFINITY;
        if (nearestPlayer != null) {
            bestSq = nearestPlayer.distanceToSqr(x, y, z);
        }

        double anchorBest = Double.POSITIVE_INFINITY;
        for (BlockPos ap : anchors) {
            double dx = ap.getX() + 0.5 - x;
            double dy = ap.getY() - y;
            double dz = ap.getZ() + 0.5 - z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d < anchorBest) anchorBest = d;
        }

        double result = Math.min(bestSq, anchorBest);
        return Double.isFinite(result) ? result : null;
    }

    @Unique
    private static final int MIN_RING = 2;
    @Unique
    private static final int MAX_RING = 8;

    @Redirect(
            method = "spawnForChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;canSpawnForCategoryLocal(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/world/level/ChunkPos;)Z"
            )
    )
    private static boolean goafk$canSpawnOrAnchor(
            NaturalSpawner.SpawnState info, MobCategory category, ChunkPos pos,
            ServerLevel level, LevelChunk chunk, NaturalSpawner.SpawnState infoArg, List<MobCategory> categories
    ) {
        boolean base = ((NaturalSpawnerSpawnStateInvoker) info).goafk$invokeCanSpawnForCategoryLocal(category, pos);
        if (base) return true;

        for (BlockPos ap : AFKManager.getAnchorPositions(level)) {
            ChunkPos center = ChunkPos.containing(ap);
            int ring = Math.max(Math.abs(pos.x() - center.x()), Math.abs(pos.z() - center.z()));
            if (ring >= MIN_RING && ring <= MAX_RING) {
                return true;
            }
        }
        return false;
    }
}
