package com.drtdrc.crdtrdsmod.mixin.trials;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrialSpawner.FullConfig.class)
public class TrialSpawnerFullConfigMixin {
    @Shadow @Final @Mutable public static TrialSpawner.FullConfig DEFAULT;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void crdtrdsmod$overrideDefaultOminousLootTable(CallbackInfo ci) {

        WeightedList<ResourceKey<LootTable>> ominousEject = WeightedList.<ResourceKey<LootTable>>builder()
                .add(BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_KEY, 3)
                .add(BuiltInLootTables.SPAWNER_OMINOUS_TRIAL_CHAMBER_CONSUMABLES, 7)
                .build();

        TrialSpawnerConfig ominousCfg = TrialSpawnerConfig.builder()
                .lootTablesToEject(ominousEject)
                .build();

        DEFAULT = new TrialSpawner.FullConfig(
                Holder.direct(TrialSpawnerConfig.DEFAULT),
                Holder.direct(ominousCfg),
                DEFAULT.targetCooldownLength(),
                DEFAULT.requiredPlayerRange()
        );
    }
}
