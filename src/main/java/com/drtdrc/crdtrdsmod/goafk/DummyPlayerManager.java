package com.drtdrc.crdtrdsmod.goafk;

import com.google.common.collect.LinkedHashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ProfileResult;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DummyPlayerManager {
    private DummyPlayerManager() {}

    private static final String AFK_TEAM = "goafk_dummies";
    public static final Set<UUID> GOING_AFK = ConcurrentHashMap.newKeySet();
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
        setupTeam(server);
        addToTeam(server, player);

        return player;
    }

    private static GameProfile resolveProfileWithSkin(MinecraftServer server, UUID fakeUuid, String name) {
        // Look up the player's real UUID directly from Mojang API.
        // We cannot use profileResolver().fetchByName() because placeNewPlayer
        // pollutes the usercache with our fake UUID, causing future lookups to
        // resolve the fake UUID instead of the real one.
        return server.services().profileRepository().findProfileByName(name)
                .map(nameAndId -> {
                    ProfileResult result = server.services().sessionService()
                            .fetchProfile(nameAndId.id(), true);
                    if (result != null) {
                        return new GameProfile(fakeUuid, name,
                                new PropertyMap(LinkedHashMultimap.create(
                                        result.profile().properties())));
                    }
                    return new GameProfile(fakeUuid, name);
                })
                .orElseGet(() -> new GameProfile(fakeUuid, name));
    }

    public static void remove(MinecraftServer server, ServerPlayer player) {
        removeFromTeam(server, player);
        server.getPlayerList().remove(player);
    }

    private static void setupTeam(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(AFK_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(AFK_TEAM);
        }
        team.setPlayerPrefix(Component.literal("(AFK) ")
                .withStyle(s -> s.withColor(0x888888).withItalic(true)));
        team.setColor(ChatFormatting.GRAY);
        team.setNameTagVisibility(Team.Visibility.ALWAYS);
    }

    private static void addToTeam(MinecraftServer server, ServerPlayer player) {
        ServerScoreboard scoreboard = server.getScoreboard();
        scoreboard.addPlayerToTeam(player.getScoreboardName(),
                Objects.requireNonNull(scoreboard.getPlayerTeam(AFK_TEAM)));
    }

    private static void removeFromTeam(MinecraftServer server, ServerPlayer player) {
        ServerScoreboard scoreboard = server.getScoreboard();
        scoreboard.removePlayerFromTeam(player.getScoreboardName(),
                Objects.requireNonNull(scoreboard.getPlayerTeam(AFK_TEAM)));
    }
}
