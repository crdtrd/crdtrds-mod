package com.drtdrc.crdtrdsmod.cocktails;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import static com.drtdrc.crdtrdsmod.core.CrdtrdsMod.MOD_ID;

public class Cocktails {
    public static void init() {
        Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "cocktails"),
                CocktailsRecipe.SERIALIZER
        );
    }
}
