package com.drtdrc.crdtrdsmod.goafk;

import com.drtdrc.crdtrdsmod.ModConfig;
import com.drtdrc.crdtrdsmod.afk.AFKAnchorsState;
import com.drtdrc.crdtrdsmod.afk.AFKCommand;
import com.drtdrc.crdtrdsmod.afk.AFKManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class GoafkInit implements ModInitializer {
    @Override
    public void onInitialize() {
        if (ModConfig.active().goAfk) {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                for (ServerLevel level : server.getAllLevels()) {
                    var anchors = AFKAnchorsState.get(level).getAllPositions();
                    int radius = AFKManager.computeRadius(server);
                    for (BlockPos pos : anchors) {
                        AFKManager.addTicketsAround(level, pos, radius);
                    }
                }
            });
            CommandRegistrationCallback.EVENT.register(AFKCommand::register);
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                AFKManager.onPlayerJoin(handler.getPlayer());
            });
        }
    }
}
