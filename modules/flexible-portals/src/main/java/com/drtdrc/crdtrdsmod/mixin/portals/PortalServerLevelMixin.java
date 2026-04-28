package com.drtdrc.crdtrdsmod.mixin.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.HashSet;

@Mixin(ServerLevel.class)
public class PortalServerLevelMixin {

    @Inject(method = "globalLevelEvent", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$endPortalOpenedLocal(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId == 1038) {
            ServerLevel self = (ServerLevel) (Object) this;
            BlockPos center = crdtrdsmod$findPortalCenter(self, pos);
            self.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
            ci.cancel();
        }
    }

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$endPortalOpenedNonGlobal(Entity entity, int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (eventId == 1038) {
            ServerLevel self = (ServerLevel) (Object) this;
            BlockPos center = crdtrdsmod$findPortalCenter(self, pos);
            self.playSound(null, center, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
            ci.cancel();
        }
    }

    @Unique
    private static BlockPos crdtrdsmod$findPortalCenter(ServerLevel level, BlockPos origin) {
        var queue = new ArrayDeque<BlockPos>();
        var visited = new HashSet<BlockPos>();

        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos start = origin.relative(d);
            if (level.getBlockState(start).is(Blocks.END_PORTAL)) {
                queue.add(start);
            }
        }
        if (queue.isEmpty()) {
            if (level.getBlockState(origin).is(Blocks.END_PORTAL)) {
                queue.add(origin);
            } else {
                return origin;
            }
        }

        double sumX = 0, sumY = 0, sumZ = 0;
        int count = 0;
        while (!queue.isEmpty() && count < 256) {
            BlockPos p = queue.poll();
            if (!visited.add(p)) continue;
            if (!level.getBlockState(p).is(Blocks.END_PORTAL)) continue;
            sumX += p.getX();
            sumY += p.getY();
            sumZ += p.getZ();
            count++;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos next = p.relative(d);
                if (!visited.contains(next)) queue.add(next);
            }
        }

        if (count == 0) return origin;
        return BlockPos.containing(sumX / count + 0.5, sumY / count + 0.5, sumZ / count + 0.5);
    }
}
