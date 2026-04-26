package com.drtdrc.crdtrdsmod.mixin.afk;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NaturalSpawner.SpawnState.class)
public interface NaturalSpawnerSpawnStateInvoker {
    @Invoker("canSpawnForCategoryLocal")
    boolean goafk$invokeCanSpawnForCategoryLocal(MobCategory category, ChunkPos pos);
}
