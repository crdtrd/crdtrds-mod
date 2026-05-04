package com.drtdrc.crdtrdsmod.cocktails;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class CocktailsInit implements ModInitializer {

    @Override
    public void onInitialize() {
        register();
        if (ModConfig.active().cocktails) {
            ModContainer container = FabricLoader.getInstance().getModContainer("crdtrdsmod_cocktails").orElseThrow();
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath("crdtrdsmod_cocktails", "cocktails_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath("crdtrdsmod", "cocktails"),
                CocktailsRecipe.SERIALIZER
        );
    }
}
