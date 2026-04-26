package com.drtdrc.crdtrdsmod.mixin.afk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(NaturalSpawner.class)
public interface NaturalSpawnerInvokers {

    @Invoker("isRightDistanceToPlayerAndSpawnPoint")
    static boolean goafk$isRightDistanceToPlayerAndSpawnPoint(ServerLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos pos, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("getRandomSpawnMobAt")
    static Optional<MobSpawnSettings.SpawnerData> goafk$getRandomSpawnMobAt(ServerLevel level, StructureManager structureManager,
                                                                            ChunkGenerator chunkGenerator, MobCategory category,
                                                                            RandomSource random, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("isValidSpawnPostitionForType")
    static boolean goafk$isValidSpawnPostitionForType(ServerLevel level, MobCategory category, StructureManager structureManager,
                                                      ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData data,
                                                      BlockPos.MutableBlockPos pos, double squaredDistance) {
        throw new AssertionError();
    }

    @Invoker("getMobForSpawn")
    @Nullable
    static Mob goafk$getMobForSpawn(ServerLevel level, EntityType<?> type) {
        throw new AssertionError();
    }

    @Invoker("isValidPositionForMob")
    static boolean goafk$isValidPositionForMob(ServerLevel level, Mob entity, double squaredDistance) {
        throw new AssertionError();
    }
}
