package com.drtdrc.crdtrdsmod.core;

import com.drtdrc.crdtrdsmod.core.resource.ModuleEnabledCondition;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CrdtrdsMod implements ModInitializer, IMixinConfigPlugin {
    public static final String MOD_ID = "crdtrdsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ResourceConditions.register(ModuleEnabledCondition.TYPE);
        LOGGER.info("crdtrd's mod initialized");
    }

    @Override
    public void onLoad(String mixinPackage) {
        ModConfig.load();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        ModConfig modConfig = ModConfig.get();
        String prefix = "crdtrds-mod.modules.";

        if (mixinClassName.contains(prefix + "cheaper-anvils") && modConfig.cheaperAnvils) return true;
        if (mixinClassName.contains(prefix + "cocktails") && modConfig.cocktails) return true;
        if (mixinClassName.contains(prefix + "curse-stone") && modConfig.curseStone) return true;
        if (mixinClassName.contains(prefix + "delimited-anvils") && modConfig.delimitedAnvils) return true;
        if (mixinClassName.contains(prefix + "enchantingencore-encore") && modConfig.enchantingEncore) return true;
        if (mixinClassName.contains(prefix + "flexible-portals") && modConfig.flexiblePortals) return true;
        if (mixinClassName.contains(prefix + "give-me-recipes") && modConfig.giveMeRecipes) return true;
        if (mixinClassName.contains(prefix + "go-afk") && modConfig.goAfk) return true;
        if (mixinClassName.contains(prefix + "mineable-bedrock") && modConfig.mineableBedrock) return true;
        if (mixinClassName.contains(prefix + "mineable-spawners") && modConfig.mineableSpawners) return true;
        if (mixinClassName.contains(prefix + "mineable-trials") && modConfig.mineableTrials) return true;
        if (mixinClassName.contains(prefix + "spawn-egg-drops") && modConfig.spawnEggDrops) return true;
        if (mixinClassName.contains(prefix + "tick-warp-sleep") && modConfig.tickWarpSleep) return true;

        return false;
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }



    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
