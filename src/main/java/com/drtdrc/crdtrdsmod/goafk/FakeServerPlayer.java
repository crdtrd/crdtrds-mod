package com.drtdrc.crdtrdsmod.goafk;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class FakeServerPlayer extends ServerPlayer {

    public FakeServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation clientInfo) {
        super(server, level, profile, clientInfo);
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
