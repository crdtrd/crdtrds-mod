package com.drtdrc.crdtrdsmod.mixin.afk;

import com.drtdrc.crdtrdsmod.afk.AFKManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class AFKServerLevelMixin {
    @Unique
    private static final int MIN_RING = 2;
    @Unique
    private static final int MAX_RING = 8;

    @Inject(method = "isNaturalSpawningAllowed(Lnet/minecraft/world/level/ChunkPos;)Z",
            at = @At("HEAD"), cancellable = true)
    private void goafk$anchorSpawnable(ChunkPos pos, CallbackInfoReturnable<Boolean> cir) {
        ServerLevel level = (ServerLevel) (Object) this;
        List<BlockPos> anchors = AFKManager.getAnchorPositions(level);
        if (anchors.isEmpty()) return;

        for (BlockPos ap : anchors) {
            ChunkPos center = ChunkPos.containing(ap);
            int ring = Math.max(Math.abs(pos.x() - center.x()), Math.abs(pos.z() - center.z()));
            if (ring >= MIN_RING && ring <= MAX_RING) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void goafk$drawAnchorParticles(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (level.getGameTime() % 10 != 0) return;

        List<BlockPos> anchors = AFKManager.getAnchorPositions(level);
        if (anchors.isEmpty()) return;
        int steps = 12;
        int count = 3;
        for (BlockPos p : anchors) {
            double x = p.getX() + 0.5, y = p.getY() + 1.2, z = p.getZ() + 0.5;
            for (int i = 0; i < steps; i++) {
                double a = (i / (double) steps) * Math.PI * 2;
                double rx = x + Math.cos(a) * 0.3;
                double rz = z + Math.sin(a) * 0.3;
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, rx, y, rz, count, 0, 0.3, 0, 0);
            }
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, count, 0.0, 0.3, 0.0, 0.0);
        }
    }
}
