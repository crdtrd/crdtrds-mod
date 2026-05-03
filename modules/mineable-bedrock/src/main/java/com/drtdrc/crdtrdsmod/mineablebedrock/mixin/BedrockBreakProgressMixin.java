package com.drtdrc.crdtrdsmod.mineablebedrock.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class BedrockBreakProgressMixin {

    @Shadow private boolean isDestroyingBlock;
    @Shadow private int destroyProgressStart;
    @Shadow private BlockPos destroyPos;
    @Shadow private int lastSentState;
    @Shadow protected ServerLevel level;
    @Shadow @Final protected ServerPlayer player;
    @Shadow private int gameTicks;

    @Unique private BlockState crdtrdsmod$bedrock_oldState;

    @Shadow protected abstract float incrementDestroyProgress(BlockState state, BlockPos pos, int startTick);
    @Shadow public abstract boolean destroyBlock(BlockPos pos);

    @Inject(method = "tick", at = @At("HEAD"))
    public void crdtrdsmod$bedrockOnUpdate(CallbackInfo ci) {
        if (!this.isDestroyingBlock) return;

        BlockState state = this.level.getBlockState(this.destroyPos);
        if (!state.is(Blocks.BEDROCK)) return;

        float f = this.incrementDestroyProgress(state, this.destroyPos, this.destroyProgressStart);
        if (f >= 1.0f) {
            this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
            this.lastSentState = -1;
            this.isDestroyingBlock = false;
            this.destroyBlock(this.destroyPos);
        }
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void crdtrdsmod$bedrockCaptureOriginal(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.crdtrdsmod$bedrock_oldState = this.level.getBlockState(pos);
    }

    @Inject(
            method = "destroyBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;",
                    shift = At.Shift.AFTER
            )
    )
    private void crdtrdsmod$bedrockOnBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (crdtrdsmod$bedrock_oldState == null || !crdtrdsmod$bedrock_oldState.is(Blocks.BEDROCK)) return;

        this.level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, crdtrdsmod$bedrock_oldState),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                10, 0.5, 0.5, 0.5, 0.1
        );

        int rawId = Block.getId(crdtrdsmod$bedrock_oldState);
        level.levelEvent(2001, pos, rawId);
    }

    @Inject(method = "incrementDestroyProgress", at = @At("RETURN"))
    private void crdtrdsmod$bedrockOnContinueMining(BlockState state, BlockPos pos, int startTime, CallbackInfoReturnable<Float> cir) {
        if (!state.is(Blocks.BEDROCK)) return;

        int ticks = this.gameTicks - startTime;
        float f = state.getDestroyProgress(this.player, this.player.level(), pos) * (ticks + 1);
        int stage = (int) (f * 10.0f);

        player.connection.send(
                new ClientboundBlockDestructionPacket(player.getId(), pos, stage)
        );
    }
}
