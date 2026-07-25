package com.drtdrc.crdtrdsmod.villageroads;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import static com.drtdrc.crdtrdsmod.core.CrdtrdsMod.MOD_ID;

public final class VillageRoads {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "village_roads");

    public static final Feature<NoneFeatureConfiguration> FEATURE = new VillageRoadsFeature(NoneFeatureConfiguration.CODEC);

    public static final ResourceKey<PlacedFeature> PLACED_FEATURE = ResourceKey.create(Registries.PLACED_FEATURE, ID);

    private VillageRoads() {
    }

    public static void init() {
        Registry.register(BuiltInRegistries.FEATURE, ID, FEATURE);

        // The configured_feature and placed_feature that reference this feature ship in the
        // "village_roads_enabled" builtin datapack. Inject the placed feature into every overworld
        // biome so road segments render regardless of the biome they cross.
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                PLACED_FEATURE
        );
    }
}
