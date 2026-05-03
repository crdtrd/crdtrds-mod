package com.drtdrc.crdtrdsmod.mineabletrials.mixin;

import com.drtdrc.crdtrdsmod.mineabletrials.VaultServerDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class TrialsBlockMixin {

    @Inject(method = "setPlacedBy", at = @At(value = "TAIL"))
    void crdtrdsmod$onPlacedVault(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (level.isClientSide()) return;
        if (!(placer instanceof Player)) return;
        if (!(state.getBlock() instanceof VaultBlock)) return;

        var be = level.getBlockEntity(pos);

        if (be instanceof VaultBlockEntity vbe) {
            var data = (VaultServerDataAccessor) (Object) vbe.getServerData();
            if (data != null) {
                data.crdtrdsmod$setGlobalCooldownTicks(data.crdtrdsmod$getCooldownTickConstant());
            }
        }
    }
}
