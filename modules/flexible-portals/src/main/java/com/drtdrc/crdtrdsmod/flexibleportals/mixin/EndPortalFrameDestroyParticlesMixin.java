package com.drtdrc.crdtrdsmod.flexibleportals.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class EndPortalFrameDestroyParticlesMixin {

    @Inject(method = "spawnDestroyParticles", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$suppressEndPortalFrameDestroyParticles(Level level, Player player, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (state.is(Blocks.END_PORTAL_FRAME)) {
            ci.cancel();
        }
    }
}
