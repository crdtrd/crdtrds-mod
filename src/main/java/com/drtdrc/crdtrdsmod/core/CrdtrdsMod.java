package com.drtdrc.crdtrdsmod.core;

import com.drtdrc.crdtrdsmod.cocktails.Cocktails;
import com.drtdrc.crdtrdsmod.goafk.GoAFK;
import com.drtdrc.crdtrdsmod.spawneggdrops.SpawnEggDrops;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ComposterBlock;
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
        LOGGER.info("crdtrd's mod initialized");

        ModConfig cfg = ModConfig.active();
        ModContainer container = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();


        // Register builtin resource packs and initialize code for enabled modules
        if (cfg.enchantingEncore) {
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "enchantingencore_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
        if (cfg.flexiblePortals) {
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "flexible_portals_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
        if (cfg.mineableBedrock) {
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "mineable_bedrock_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
        if (cfg.mineableReinforcedDeepslate) {
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "mineable_reinforced_deepslate_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
        if (cfg.mineableSpawners) {
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "mineable_spawners_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
        if (cfg.mineableTrials) {
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "mineable_trials_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }
        if (cfg.cocktails) {
            Cocktails.init();
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MOD_ID, "cocktails_enabled"), container, PackActivationType.ALWAYS_ENABLED);
        }

        // CompostableFlesh
        if (cfg.compostableFlesh) {
            ComposterBlock.COMPOSTABLES.put(Items.ROTTEN_FLESH, 0.3f);
        }

        // SpawnEggDrops
        if (cfg.spawnEggDrops) {
            SpawnEggDrops.register();
        }

        // GoAFK
        if (cfg.goAfk) {
            GoAFK.init();
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        ModConfig.load();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        ModConfig modConfig = ModConfig.get();
        String prefix = "com.drtdrc.crdtrdsmod.";

        if (mixinClassName.contains(prefix + "cheaperanvils") && modConfig.cheaperAnvils) return true;
        if (mixinClassName.contains(prefix + "cocktails") && modConfig.cocktails) return true;
        if (mixinClassName.contains(prefix + "cursestone") && modConfig.curseStone) return true;
        if (mixinClassName.contains(prefix + "delimitedanvils") && modConfig.delimitedAnvils) return true;
        if (mixinClassName.contains(prefix + "enchantingencore") && modConfig.enchantingEncore) return true;
        if (mixinClassName.contains(prefix + "flexibleportals") && modConfig.flexiblePortals) return true;
        if (mixinClassName.contains(prefix + "givemerecipes") && modConfig.giveMeRecipes) return true;
        if (mixinClassName.contains(prefix + "goafk") && modConfig.goAfk) return true;
        if (mixinClassName.contains(prefix + "mineablebedrock") && modConfig.mineableBedrock) return true;
        if (mixinClassName.contains(prefix + "mineablespawners") && modConfig.mineableSpawners) return true;
        if (mixinClassName.contains(prefix + "mineabletrials") && modConfig.mineableTrials) return true;
        if (mixinClassName.contains(prefix + "spawneggdrops") && modConfig.spawnEggDrops) return true;
        if (mixinClassName.contains(prefix + "tickwarpsleep") && modConfig.tickWarpSleep) return true;
        if (mixinClassName.contains(prefix + "mineablereinforceddeepslate") && modConfig.mineableReinforcedDeepslate) return true;

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
