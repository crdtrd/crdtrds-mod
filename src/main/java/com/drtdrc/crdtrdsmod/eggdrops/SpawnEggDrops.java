package com.drtdrc.crdtrdsmod.eggdrops;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Optional;

public class SpawnEggDrops {

    private static final float BASE_CHANCE = 0.005f;

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!ModConfig.get().spawnEggDrops) return;
            if (source.isBuiltin()) {
                String path = key.identifier().getPath();
                if (path.startsWith("entities/")) {
                    String entityName = path.substring("entities/".length());
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE
                            .getOptional(net.minecraft.resources.Identifier.withDefaultNamespace(entityName))
                            .orElse(null);
                    if (entityType == null) return;

                    Optional<Holder<Item>> eggHolder = SpawnEggItem.byId(entityType);
                    if (eggHolder.isEmpty()) return;

                    LootPool.Builder pool = LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(eggHolder.get().value()))
                            .when(LootItemRandomChanceWithEnchantedBonusCondition
                                    .randomChanceAndLootingBoost(registries, BASE_CHANCE, BASE_CHANCE));

                    tableBuilder.withPool(pool);
                }
            }
        });
    }
}
