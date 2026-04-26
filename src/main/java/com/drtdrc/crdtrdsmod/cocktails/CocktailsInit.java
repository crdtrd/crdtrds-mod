package com.drtdrc.crdtrdsmod.cocktails;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class CocktailsInit {

    public static void register() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath("crdtrdsmod", "cocktails"),
                CocktailsRecipe.SERIALIZER
        );
    }
}
