package com.drtdrc.crdtrdsmod.mineablespawners.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(SpawnerBlock.class)
public abstract class SpawnerBlockMixin {

    @Inject(method = "spawnAfterBreak", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$silkTouchSpawner(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience, CallbackInfo ci) {
        if (!ModConfig.active().mineableSpawners) return;
        Optional<Holder.Reference<Enchantment>> silkTouch = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.SILK_TOUCH);
        if (silkTouch.isPresent() && EnchantmentHelper.getItemEnchantmentLevel(silkTouch.get(), tool) > 0) {
            ci.cancel();
        }
    }
}
