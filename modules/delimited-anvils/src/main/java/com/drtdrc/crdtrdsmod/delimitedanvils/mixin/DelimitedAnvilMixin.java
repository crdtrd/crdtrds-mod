package com.drtdrc.crdtrdsmod.delimitedanvils.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AnvilMenu.class)
public abstract class DelimitedAnvilMixin {

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int crdtrdsmod$removeAnvilLimit(int original) {
        return Integer.MAX_VALUE;
    }
}
