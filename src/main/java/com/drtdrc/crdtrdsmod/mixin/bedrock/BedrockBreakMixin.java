package com.drtdrc.crdtrdsmod.mixin.bedrock;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class BedrockBreakMixin {

    @Shadow
    protected ServerLevel level;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$preventBedrockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.get().mineableBedrock && level.getBlockState(pos).is(Blocks.BEDROCK)) {
            cir.setReturnValue(false);
        }
    }
}
