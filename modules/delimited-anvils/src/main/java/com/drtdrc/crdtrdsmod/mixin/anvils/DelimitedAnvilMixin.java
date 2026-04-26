package com.drtdrc.crdtrdsmod.mixin.anvils;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public abstract class DelimitedAnvilMixin {

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int crdtrdsmod$removeAnvilLimit(int original) {
        if (!ModConfig.active().delimitedAnvils) return original;
        return Integer.MAX_VALUE;
    }
}
