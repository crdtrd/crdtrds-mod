package com.drtdrc.crdtrdsmod.flexibleportals.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import com.drtdrc.crdtrdsmod.flexibleportals.PortalsUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PortalShape.class)
public abstract class NetherPortalMixin {

    @Shadow @Final @Mutable
    private static BlockBehaviour.StatePredicate FRAME;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void crdtrdsmod$expandValidFrame(CallbackInfo ci) {
        if (!ModConfig.get().flexiblePortals) return;
        FRAME = (state, level, pos) ->
                state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN);
    }

    @Inject(method = "findEmptyPortalShape", at = @At("HEAD"), cancellable = true)
    private static void crdtrdsmod$freeformCreateFirst(
            LevelAccessor level, BlockPos pos, Direction.Axis firstCheckedAxis,
            CallbackInfoReturnable<Optional<PortalShape>> cir) {
        if (!ModConfig.active().flexiblePortals) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        boolean created = PortalsUtil.findAndCreate(serverLevel, pos, PortalsUtil.PortalSpec.nether(), null);

        if (created) {
            cir.setReturnValue(Optional.empty());
            cir.cancel();
        }
    }
}
