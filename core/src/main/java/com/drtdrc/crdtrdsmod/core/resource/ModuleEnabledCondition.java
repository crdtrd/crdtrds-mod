package com.drtdrc.crdtrdsmod.core.resource;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;

public record ModuleEnabledCondition(String module) implements ResourceCondition {

    public static final MapCodec<ModuleEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    com.mojang.serialization.Codec.STRING.fieldOf("module").forGetter(ModuleEnabledCondition::module)
            ).apply(instance, ModuleEnabledCondition::new)
    );

    public static final ResourceConditionType<ModuleEnabledCondition> TYPE =
            ResourceConditionType.create(
                    Identifier.fromNamespaceAndPath("crdtrdsmod", "module_enabled"),
                    CODEC
            );

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(RegistryOps.RegistryInfoLookup lookup) {
        ModConfig cfg = ModConfig.active();
        return switch (module) {
            case "enchantingEncore" -> cfg.enchantingEncore;
            case "flexiblePortals" -> cfg.flexiblePortals;
            case "goAfk" -> cfg.goAfk;
            case "tickWarpSleep" -> cfg.tickWarpSleep;
            case "mineableTrials" -> cfg.mineableTrials;
            case "mineableBedrock" -> cfg.mineableBedrock;
            case "cocktails" -> cfg.cocktails;
            case "cheaperAnvils" -> cfg.cheaperAnvils;
            case "mineableSpawners" -> cfg.mineableSpawners;
            case "spawnEggDrops" -> cfg.spawnEggDrops;
            case "delimitedAnvils" -> cfg.delimitedAnvils;
            case "giveMeRecipes" -> cfg.giveMeRecipes;
            case "curseStone" -> cfg.curseStone;
            default -> true;
        };
    }
}
