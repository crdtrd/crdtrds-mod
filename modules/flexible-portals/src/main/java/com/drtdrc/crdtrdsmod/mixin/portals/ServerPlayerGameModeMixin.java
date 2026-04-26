package com.drtdrc.crdtrdsmod.mixin.portals;

import com.drtdrc.crdtrdsmod.ModConfig;
import com.drtdrc.crdtrdsmod.portals.PortalsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow
    protected ServerLevel level;

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void crdtrdsmod$onDestroyPortalFrame(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.active().flexiblePortals) return;
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.END_PORTAL_FRAME)) {
            PortalsUtil.removeNearbyPortalBlocks(level, pos);
        }
    }
}
