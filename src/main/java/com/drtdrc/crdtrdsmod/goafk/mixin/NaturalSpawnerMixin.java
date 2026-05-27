package com.drtdrc.crdtrdsmod.goafk.mixin;

import com.drtdrc.crdtrdsmod.goafk.AFKManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
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
    private static void goafk$onSpawnCategoryForPosition(MobCategory mobCategory, ServerLevel level, ChunkAccess chunk, BlockPos start, NaturalSpawner.SpawnPredicate extraTest, NaturalSpawner.AfterSpawnCallback spawnCallback, CallbackInfo ci) {
        List<BlockPos> anchors = AFKManager.getAnchorPositions(level);
        if (anchors.isEmpty()) return;

        StructureManager structureManager = level.structureManager();
        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        int yStart = start.getY();
        BlockState state = chunk.getBlockState(start);
        if (state.isRedstoneConductor(chunk, start)) {
            ci.cancel();
            return;
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int clusterSize = 0;

        for (int groupCount = 0; groupCount < 3; groupCount++) {
            int x = start.getX();
            int z = start.getZ();
            MobSpawnSettings.SpawnerData currentSpawnData = null;
            SpawnGroupData groupData = null;
            int max = Mth.ceil(level.getRandom().nextFloat() * 4.0F);
            int groupSize = 0;

            for (int ll = 0; ll < max; ll++) {
                x += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
                z += level.getRandom().nextInt(6) - level.getRandom().nextInt(6);
                pos.set(x, yStart, z);

                double xx = x + 0.5;
                double zz = z + 0.5;

                // factoring in afk anchors as well as players, instead of just players
                Double nearestPlayerOrAnchorDistanceSq = goafk$getNearestPlayerOrAnchorSq(level, anchors, xx, yStart, zz);
                // replacing vanilla check for existence of nearest player with existence of nearest distance value
                if (nearestPlayerOrAnchorDistanceSq == null) {
                    continue;
                }


                if (NaturalSpawnerInvokers.goafk$isRightDistanceToPlayerAndSpawnPoint(level, chunk, pos, nearestPlayerOrAnchorDistanceSq)) {
                    if (currentSpawnData == null) {
                        Optional<MobSpawnSettings.SpawnerData> nextSpawnData = NaturalSpawnerInvokers.goafk$getRandomSpawnMobAt(level, structureManager, chunkGenerator, mobCategory, level.getRandom(), pos);
                        if (nextSpawnData.isEmpty()) {
                            continue;
                        }
                        currentSpawnData = nextSpawnData.get();
                        max = currentSpawnData.minCount() + level.getRandom().nextInt(1 + currentSpawnData.maxCount() - currentSpawnData.minCount());
                    }

                    if (NaturalSpawnerInvokers.goafk$isValidSpawnPostitionForType(level, mobCategory, structureManager, chunkGenerator, currentSpawnData, pos, nearestPlayerOrAnchorDistanceSq)
                            && extraTest.test(currentSpawnData.type(), pos, chunk)) {

                        Mob mob = NaturalSpawnerInvokers.goafk$getMobForSpawn(level, currentSpawnData.type());
                        if (mob == null) {
                            ci.cancel();
                            return;
                        }

                        mob.snapTo(xx, (double) yStart, zz, level.getRandom().nextFloat() * 360.0F, 0.0F);
                        if (NaturalSpawnerInvokers.goafk$isValidPositionForMob(level, mob, nearestPlayerOrAnchorDistanceSq)) {
                            groupData = mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.NATURAL, groupData);
                            clusterSize++;
                            groupSize++;
                            level.addFreshEntityWithPassengers(mob);
                            spawnCallback.run(mob, chunk);

                            if (clusterSize >= mob.getMaxSpawnClusterSize()) {
                                ci.cancel();
                                return;
                            }
                            if (mob.isMaxGroupSizeReached(groupSize)) {
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
        // vanilla getNearestPlayer behavior
        Player nearestPlayer = level.getNearestPlayer(x, y, z, -1.0, false);
        double bestSq = Double.POSITIVE_INFINITY;
        if (nearestPlayer != null) {
            bestSq = nearestPlayer.distanceToSqr(x, y, z);
        }

        // also see if there is a closer afk anchor
        double anchorBest = Double.POSITIVE_INFINITY;
        for (BlockPos ap : anchors) {
            double dx = ap.getX() + 0.5 - x;
            double dy = ap.getY() - y;
            double dz = ap.getZ() + 0.5 - z;
            double d = dx * dx + dy * dy + dz * dz;
            if (d < anchorBest) anchorBest = d;
        }

        // compare nearest player distance with nearest anchor distance and choose smallest.
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
