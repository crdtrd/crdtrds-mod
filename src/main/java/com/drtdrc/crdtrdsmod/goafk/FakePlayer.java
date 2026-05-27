package com.drtdrc.crdtrdsmod.goafk;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class FakePlayer {
    private FakePlayer() {}

    public static ServerPlayer spawn(MinecraftServer server, ServerLevel level, BlockPos pos, String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("GoAFK:" + name).getBytes(StandardCharsets.UTF_8));
        GameProfile profile = new GameProfile(uuid, name);

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);

        ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);

        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);

        return player;
    }

    public static void remove(MinecraftServer server, ServerPlayer player) {
        server.getPlayerList().remove(player);
    }
}
