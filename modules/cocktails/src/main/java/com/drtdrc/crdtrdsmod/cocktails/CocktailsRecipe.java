package com.drtdrc.crdtrdsmod.cocktails;

import com.drtdrc.crdtrdsmod.ModConfig;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CocktailsRecipe extends CustomRecipe {

    public static final CocktailsRecipe INSTANCE = new CocktailsRecipe();
    public static final MapCodec<CocktailsRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, CocktailsRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<CocktailsRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!ModConfig.active().cocktails) return false;
        int potionCount = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents != null && !contents.equals(PotionContents.EMPTY)) {
                    potionCount++;
                }
            } else {
                return false;
            }
        }
        return potionCount >= 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        List<MobEffectInstance> allEffects = new ArrayList<>();
        boolean hasSplash = false;
        boolean hasLingering = false;
        int colorR = 0, colorG = 0, colorB = 0, colorCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.SPLASH_POTION)) hasSplash = true;
            if (stack.is(Items.LINGERING_POTION)) hasLingering = true;

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents == null) continue;
            for (MobEffectInstance effect : contents.getAllEffects()) {
                allEffects.add(new MobEffectInstance(effect));
                int color = effect.getEffect().value().getColor();
                colorR += (color >> 16) & 0xFF;
                colorG += (color >> 8) & 0xFF;
                colorB += color & 0xFF;
                colorCount++;
            }
        }

        ItemStack result;
        if (hasLingering) {
            result = new ItemStack(Items.LINGERING_POTION);
        } else if (hasSplash) {
            result = new ItemStack(Items.SPLASH_POTION);
        } else {
            result = new ItemStack(Items.POTION);
        }

        int blendedColor = -1;
        if (colorCount > 0) {
            blendedColor = ((colorR / colorCount) << 16) | ((colorG / colorCount) << 8) | (colorB / colorCount);
        }

        PotionContents contents = new PotionContents(
                Optional.empty(),
                colorCount > 0 ? Optional.of(blendedColor) : Optional.empty(),
                allEffects,
                Optional.empty()
        );
        result.set(DataComponents.POTION_CONTENTS, contents);

        String name = hasLingering ? "Lingering Cocktail" : hasSplash ? "Splash Cocktail" : "Cocktail";
        result.set(DataComponents.CUSTOM_NAME,
                Component.literal(name).withStyle(s -> s.withItalic(false)));

        return result;
    }

    @Override
    public RecipeSerializer<CocktailsRecipe> getSerializer() {
        return SERIALIZER;
    }
}
