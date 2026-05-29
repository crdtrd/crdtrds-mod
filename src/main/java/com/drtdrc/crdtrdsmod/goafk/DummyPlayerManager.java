package com.drtdrc.crdtrdsmod.goafk;

import com.google.common.collect.LinkedHashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import io.netty.channel.embedded.EmbeddedChannel;
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

public final class DummyPlayerManager {
    private DummyPlayerManager() {}

    public static UUID fakeUUID(String name) {
        return UUID.nameUUIDFromBytes(("GoAFK:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isDummy(ServerPlayer player) {
        return player.getUUID().equals(fakeUUID(player.getGameProfile().name()));
    }

    public static ServerPlayer spawn(MinecraftServer server, ServerLevel level,
                                     double x, double y, double z, String name,
                                     float yaw, float pitch) {
        UUID uuid = fakeUUID(name);
        GameProfile profile = resolveProfileWithSkin(server, uuid, name);

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
        player.snapTo(x, y, z, yaw, pitch);

        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        player.setInvulnerable(true);

        server.getPlayerList().placeNewPlayer(connection, player, cookie);

        return player;
    }

    private static GameProfile resolveProfileWithSkin(MinecraftServer server, UUID fakeUuid, String name) {
        // Look up the player's real UUID directly from Mojang API.
        // We cannot use profileResolver().fetchByName() because placeNewPlayer
        // pollutes the usercache with our fake UUID, causing future lookups to
        // resolve the fake UUID instead of the real one.
        String afkName = "(AFK) " + name;
        return server.services().profileRepository().findProfileByName(name)
                .map(nameAndId -> {
                    ProfileResult result = server.services().sessionService()
                            .fetchProfile(nameAndId.id(), true);
                    if (result != null) {
                        return new GameProfile(fakeUuid, afkName,
                                new PropertyMap(LinkedHashMultimap.create(
                                        result.profile().properties())));
                    }
                    return new GameProfile(fakeUuid, afkName);
                })
                .orElseGet(() -> new GameProfile(fakeUuid, afkName));
    }

    public static void remove(MinecraftServer server, ServerPlayer player) {
        server.getPlayerList().remove(player);
    }
}
