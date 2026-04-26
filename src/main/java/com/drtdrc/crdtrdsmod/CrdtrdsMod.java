package com.drtdrc.crdtrdsmod;

import com.drtdrc.crdtrdsmod.afk.AFKCommand;
import com.drtdrc.crdtrdsmod.cocktails.CocktailsInit;
import com.drtdrc.crdtrdsmod.eggdrops.SpawnEggDrops;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrdtrdsMod implements ModInitializer {
    public static final String MOD_ID = "crdtrdsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();

        if (ModConfig.get().cocktails) {
            CocktailsInit.register();
        }

        if (ModConfig.get().spawnEggDrops) {
            SpawnEggDrops.register();
        }

        if (ModConfig.get().goAfk) {
            CommandRegistrationCallback.EVENT.register(AFKCommand::register);

            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                com.drtdrc.crdtrdsmod.afk.AFKManager.onPlayerJoin(handler.getPlayer());
            });
        }

        LOGGER.info("crdtrd's mod initialized");
    }
}
