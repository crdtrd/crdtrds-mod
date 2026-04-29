package com.drtdrc.crdtrdsmod.flexibleportals.mixin;

import com.drtdrc.crdtrdsmod.flexibleportals.PortalsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$onUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        //if (!ModConfig.active().flexiblePortals) return;

        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(Blocks.END_PORTAL_FRAME) || blockState.getValue(EndPortalFrameBlock.HAS_EYE)) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }
        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }
        BlockState blockState2 = blockState.setValue(EndPortalFrameBlock.HAS_EYE, true);
        Block.pushEntitiesUp(blockState, blockState2, level, blockPos);
        level.setBlock(blockPos, blockState2, Block.UPDATE_CLIENTS);
        level.updateNeighbourForOutputSignal(blockPos, Blocks.END_PORTAL_FRAME);
        context.getItemInHand().shrink(1);
        level.levelEvent(1503, blockPos, 0);

        PortalsUtil.findAndCreate((ServerLevel) level, blockPos, PortalsUtil.PortalSpec.end(), SoundEvents.END_PORTAL_SPAWN);

        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
