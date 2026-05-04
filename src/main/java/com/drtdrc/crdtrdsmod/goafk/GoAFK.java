package com.drtdrc.crdtrdsmod.goafk;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class GoAFK {
    public static void init() {
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
