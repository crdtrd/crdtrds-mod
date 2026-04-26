package com.drtdrc.crdtrdsmod.mixin.recipes;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Set;

@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookMixin {

    @Shadow
    protected Set<ResourceKey<Recipe<?>>> known;

    @Inject(method = "sendInitialRecipeBook", at = @At("HEAD"))
    private void crdtrdsmod$unlockAll(ServerPlayer player, CallbackInfo ci) {
        if (!ModConfig.active().giveMeRecipes) return;

        Collection<RecipeHolder<?>> allRecipes = player.level().getServer().getRecipeManager().getRecipes();
        for (RecipeHolder<?> recipe : allRecipes) {
            this.known.add(recipe.id());
        }
    }
}
