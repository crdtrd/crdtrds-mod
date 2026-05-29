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
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class FakePlayer {
    private FakePlayer() {}

    public static final String TEAM_NAME = "goafk";

    public static UUID fakeUUID(String name) {
        return UUID.nameUUIDFromBytes(("GoAFK:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isFake(net.minecraft.world.entity.player.Player player) {
        return player.getUUID().equals(fakeUUID(player.getGameProfile().name()));
    }

    public static PlayerTeam getOrCreateTeam(Scoreboard scoreboard) {
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.addPlayerTeam(TEAM_NAME);
            team.setSeeFriendlyInvisibles(true);
            team.setCollisionRule(Team.CollisionRule.ALWAYS);
        }
        return team;
    }

    public static void ensureOnTeam(Scoreboard scoreboard, String playerName) {
        PlayerTeam team = scoreboard.getPlayerTeam(TEAM_NAME);
        if (team != null && scoreboard.getPlayersTeam(playerName) == null) {
            scoreboard.addPlayerToTeam(playerName, team);
        }
    }

    public static ServerPlayer spawn(MinecraftServer server, ServerLevel level, BlockPos pos, String name) {
        UUID uuid = fakeUUID(name);
        GameProfile profile = new GameProfile(uuid, name);

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);

        ServerPlayer player = new FakeServerPlayer(server, level, profile, ClientInformation.createDefault());
        player.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);

        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        player.setInvulnerable(true);
        player.setInvisible(true);

        server.getPlayerList().placeNewPlayer(connection, player, cookie);

        Scoreboard scoreboard = level.getScoreboard();
        PlayerTeam team = getOrCreateTeam(scoreboard);
        scoreboard.addPlayerToTeam(name, team);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!isFake(p) && scoreboard.getPlayersTeam(p.getScoreboardName()) == null) {
                scoreboard.addPlayerToTeam(p.getScoreboardName(), team);
            }
        }

        return player;
    }

    public static void remove(MinecraftServer server, ServerPlayer player) {
        server.getPlayerList().remove(player);
    }
}
