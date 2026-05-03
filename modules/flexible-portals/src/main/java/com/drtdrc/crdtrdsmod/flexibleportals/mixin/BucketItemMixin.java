package com.drtdrc.crdtrdsmod.flexibleportals.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import com.drtdrc.crdtrdsmod.flexibleportals.PortalsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @Redirect(
            method = "emptyContents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"
            )
    )
    private boolean crdtrdsmod$redirectBreakForPortal(Level level, BlockPos pos, boolean drop) {

        BlockState state = level.getBlockState(pos);

        if (!level.isClientSide()) {
            if (state.is(Blocks.END_PORTAL)) {
                PortalsUtil.breakConnectedEndPortal((ServerLevel) level, pos);
                return true;
            }
            if (state.is(Blocks.NETHER_PORTAL)) {
                PortalsUtil.breakConnectedNetherPortal((ServerLevel) level, pos);
                return true;
            }
        }

        return level.destroyBlock(pos, drop);
    }
}
