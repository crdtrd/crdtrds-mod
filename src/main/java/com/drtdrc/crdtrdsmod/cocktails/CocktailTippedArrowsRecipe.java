package com.drtdrc.crdtrdsmod.cocktails;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class CocktailTippedArrowsRecipe extends CustomRecipe {

    public static final CocktailTippedArrowsRecipe INSTANCE = new CocktailTippedArrowsRecipe();
    public static final MapCodec<CocktailTippedArrowsRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, CocktailTippedArrowsRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<CocktailTippedArrowsRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput input, @NonNull Level level) {
        if (input.width() != 3 || input.height() != 3) return false;

        ItemStack center = input.getItem(1, 1);
        if (!center.is(Items.LINGERING_POTION)) return false;

        // Must be a cocktail — has custom name "Lingering Cocktail"
        Component name = center.get(DataComponents.CUSTOM_NAME);
        if (name == null || !name.getString().equals("Lingering Cocktail")) return false;

        // All other 8 slots must be arrows
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (row == 1 && col == 1) continue;
                if (!input.getItem(col, row).is(Items.ARROW)) return false;
            }
        }
        return true;
    }

    @Override
    @NonNull
    public ItemStack assemble(CraftingInput input) {
        ItemStack potion = input.getItem(1, 1);
        PotionContents contents = potion.get(DataComponents.POTION_CONTENTS);

        ItemStack result = new ItemStack(Items.TIPPED_ARROW, 8);
        if (contents != null) {
            result.set(DataComponents.POTION_CONTENTS, contents);
        }
        result.set(DataComponents.CUSTOM_NAME,
                Component.literal("Cocktail Tipped Arrow")
                        .withStyle(s -> s.withItalic(false)));

        return result;
    }

    @Override
    @NonNull
    public RecipeSerializer<CocktailTippedArrowsRecipe> getSerializer() {
        return SERIALIZER;
    }
}
