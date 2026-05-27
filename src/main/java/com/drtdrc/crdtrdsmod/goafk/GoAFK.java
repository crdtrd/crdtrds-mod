package com.drtdrc.crdtrdsmod.goafk;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class GoAFK {
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(AFKManager::restoreFakePlayers);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> AFKManager.clearFakePlayers());
        CommandRegistrationCallback.EVENT.register(AFKCommand::register);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            AFKManager.onPlayerJoin(handler.getPlayer());
        });
    }
}
