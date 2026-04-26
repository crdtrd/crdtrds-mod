package com.drtdrc.crdtrdsmod;

import com.drtdrc.crdtrdsmod.resource.ModuleEnabledCondition;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrdtrdsMod implements ModInitializer {
    public static final String MOD_ID = "crdtrdsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ResourceConditions.register(ModuleEnabledCondition.TYPE);
        ModConfig.load();
        LOGGER.info("crdtrd's mod initialized");
    }
}
