package com.drtdrc.crdtrdsmod.goafk;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class FakePlayer {
    private FakePlayer() {}

    public static UUID fakeUUID(String name) {
        return UUID.nameUUIDFromBytes(("GoAFK:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isFake(ServerPlayer player) {
        return player.getUUID().equals(fakeUUID(player.getGameProfile().name()));
    }

    public static ServerPlayer spawn(MinecraftServer server, ServerLevel level, BlockPos pos, String name) {
        UUID uuid = fakeUUID(name);
        GameProfile profile = server.services().profileResolver().fetchByName(name)
                .map(resolved -> new GameProfile(uuid, name, resolved.properties()))
                .orElseGet(() -> new GameProfile(uuid, name));

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);

        int allSkinParts = 0;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            allSkinParts |= part.getMask();
        }
        ClientInformation clientInfo = new ClientInformation(
                "en_us", 2, ChatVisiblity.FULL, true, allSkinParts,
                HumanoidArm.RIGHT, false, false, ParticleStatus.ALL);

        ServerPlayer player = new ServerPlayer(server, level, profile, clientInfo);
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);

        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        player.setInvulnerable(true);

        server.getPlayerList().placeNewPlayer(connection, player, cookie);

        return player;
    }

    public static void remove(MinecraftServer server, ServerPlayer player) {
        server.getPlayerList().remove(player);
    }
}
