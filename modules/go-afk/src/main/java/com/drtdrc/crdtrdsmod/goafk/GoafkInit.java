package com.drtdrc.crdtrdsmod.goafk;

import com.drtdrc.crdtrdsmod.ModConfig;
import com.drtdrc.crdtrdsmod.afk.AFKCommand;
import com.drtdrc.crdtrdsmod.afk.AFKManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class GoafkInit implements ModInitializer {
    @Override
    public void onInitialize() {
        if (ModConfig.active().goAfk) {
            CommandRegistrationCallback.EVENT.register(AFKCommand::register);
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                AFKManager.onPlayerJoin(handler.getPlayer());
            });
        }
    }
}
