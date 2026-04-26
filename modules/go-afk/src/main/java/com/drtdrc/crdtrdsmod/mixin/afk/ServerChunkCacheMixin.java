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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
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

    @Inject(
            method = "tickChunks(Lnet/minecraft/util/profiling/ProfilerFiller;J)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;createState(ILjava/lang/Iterable;Lnet/minecraft/world/level/NaturalSpawner$ChunkGetter;Lnet/minecraft/world/level/LocalMobCapCalculator;)Lnet/minecraft/world/level/NaturalSpawner$SpawnState;"
            )
    )
    private void goafk$appendAnchorSpawningChunks(ProfilerFiller profiler, long timeDelta, CallbackInfo ci) {
        List<BlockPos> anchors = AFKManager.getAnchorPositions(this.level);
        if (anchors.isEmpty()) return;

        Set<Long> existing = new HashSet<>();
        for (LevelChunk lc : this.spawningChunks) {
            existing.add(lc.getPos().pack());
        }

        List<LevelChunk> toAdd = new ArrayList<>();
        for (BlockPos ap : anchors) {
            ChunkPos center = ChunkPos.containing(ap);
            for (int dx = -MAX_RING; dx <= MAX_RING; dx++) {
                for (int dz = -MAX_RING; dz <= MAX_RING; dz++) {
                    int ring = Math.max(Math.abs(dx), Math.abs(dz));
                    if (ring < MIN_RING || ring > MAX_RING) continue;
                    LevelChunk wc = ((ServerChunkCache) (Object) this).getChunkNow(center.x() + dx, center.z() + dz);
                    if (wc != null && existing.add(wc.getPos().pack())) {
                        toAdd.add(wc);
                    }
                }
            }
        }

        if (!toAdd.isEmpty()) {
            List<LevelChunk> combined = new ArrayList<>(this.spawningChunks);
            combined.addAll(toAdd);
            this.spawningChunks = combined;
        }
    }
}
