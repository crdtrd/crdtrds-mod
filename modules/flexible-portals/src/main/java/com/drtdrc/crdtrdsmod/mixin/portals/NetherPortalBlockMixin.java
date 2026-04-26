package com.drtdrc.crdtrdsmod.mixin.portals;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$onUpdateShape(
            BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState,
            RandomSource random, CallbackInfoReturnable<BlockState> cir
    ) {
        if (ModConfig.active().flexiblePortals) {
            cir.setReturnValue(state);
        }
    }
}
