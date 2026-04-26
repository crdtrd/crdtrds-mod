package com.drtdrc.crdtrdsmod.eggdrops;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
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

                    Identifier eggId = Identifier.withDefaultNamespace(entityName + "_spawn_egg");
                    Optional<Item> eggItem = BuiltInRegistries.ITEM.getOptional(eggId);
                    if (eggItem.isEmpty()) return;

                    LootPool.Builder pool = LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(eggItem.get()))
                            .when(LootItemRandomChanceWithEnchantedBonusCondition
                                    .randomChanceAndLootingBoost(registries, BASE_CHANCE, BASE_CHANCE));

                    tableBuilder.withPool(pool);
                }
            }
        });
    }
}
