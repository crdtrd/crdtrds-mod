package com.drtdrc.crdtrdsmod.mixin.afk;

import com.drtdrc.crdtrdsmod.afk.AFKManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(ServerChunkCache.class)
public abstract class ServerChunkCacheMixin {
    @Final
    @Shadow private ServerLevel level;

    @Unique
    private static final int MIN_RING = 2;
    @Unique
    private static final int MAX_RING = 8;
    @Final
    @Mutable
    @Shadow private List<LevelChunk> spawningChunks;

    @ModifyArg(
            method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;createState(ILjava/lang/Iterable;Lnet/minecraft/world/level/NaturalSpawner$ChunkGetter;Lnet/minecraft/world/level/LocalMobCapCalculator;)Lnet/minecraft/world/level/NaturalSpawner$SpawnState;"
            ),
            index = 0
    )
    private int goafk$increaseSpawningCount(int spawningChunkCount) {
        return spawningChunkCount + countAnchorSpawningChunks();
    }

    @Unique
    private int countAnchorSpawningChunks() {
        List<BlockPos> anchors = AFKManager.getAnchorPositions(this.level);
        if (anchors.isEmpty()) return 0;

        Set<Long> unique = new HashSet<>();
        for (BlockPos ap : anchors) {
            ChunkPos center = ChunkPos.containing(ap);
            for (int dx = -MAX_RING; dx <= MAX_RING; dx++) {
                for (int dz = -MAX_RING; dz <= MAX_RING; dz++) {
                    int ring = Math.max(Math.abs(dx), Math.abs(dz));
                    if (ring < MIN_RING || ring > MAX_RING) continue;
                    LevelChunk wc = ((ServerChunkCache) (Object) this).getChunkNow(center.x() + dx, center.z() + dz);
                    if (wc != null) unique.add(wc.getPos().pack());
                }
            }
        }
        return unique.size();
    }

    @Inject(
            method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;spawnForChunk(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/NaturalSpawner$SpawnState;Ljava/util/List;)V"
            )
    )
    private void goafk$appendAnchorSpawningChunks(ProfilerFiller profiler, long timeDelta, CallbackInfo ci) {
        List<BlockPos> anchors = AFKManager.getAnchorPositions(this.level);
        if (anchors.isEmpty()) return;

        for (BlockPos ap : anchors) {
            ChunkPos center = ChunkPos.containing(ap);
            for (int dx = -MAX_RING; dx <= MAX_RING; dx++) {
                for (int dz = -MAX_RING; dz <= MAX_RING; dz++) {
                    int ring = Math.max(Math.abs(dx), Math.abs(dz));
                    if (ring < MIN_RING || ring > MAX_RING) continue;
                    LevelChunk wc = ((ServerChunkCache) (Object) this).getChunkNow(center.x() + dx, center.z() + dz);
                    if (wc != null && !this.spawningChunks.contains(wc)) {
                        this.spawningChunks.add(wc);
                    }
                }
            }
        }
    }
}
