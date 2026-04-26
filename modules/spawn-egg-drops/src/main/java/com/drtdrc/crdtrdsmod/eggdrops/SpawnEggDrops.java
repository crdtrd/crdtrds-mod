package com.drtdrc.crdtrdsmod.eggdrops;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Optional;

public class SpawnEggDrops {

    private static final float BASE_CHANCE = 0.005f;
    private static final float LOOTING_PER_LEVEL = 0.005f;

    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!ModConfig.active().spawnEggDrops) return;
            if (source.isBuiltin()) {
                String path = key.identifier().getPath();
                if (path.startsWith("entities/")) {
                    String entityName = path.substring("entities/".length());

                    Identifier eggId = Identifier.withDefaultNamespace(entityName + "_spawn_egg");
                    Optional<Item> eggItem = BuiltInRegistries.ITEM.getOptional(eggId);
                    if (eggItem.isEmpty()) return;

                    LootPool.Builder pool = LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .when(LootItemKilledByPlayerCondition.killedByPlayer())
                            .when(LootItemRandomChanceWithEnchantedBonusCondition
                                    .randomChanceAndLootingBoost(registries, BASE_CHANCE, LOOTING_PER_LEVEL))
                            .add(LootItem.lootTableItem(eggItem.get()));

                    tableBuilder.withPool(pool);
                }
            }
        });
    }
}
