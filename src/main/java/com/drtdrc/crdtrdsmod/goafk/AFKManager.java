package com.drtdrc.crdtrdsmod.goafk;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AFKManager {
    private AFKManager() {}

    private static final Map<String, ServerPlayer> activeFakePlayers = new HashMap<>();

    public static String getDefaultName(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public static boolean goAFKAndKick(@NotNull ServerPlayer player) {
        ServerLevel level = player.level();
        String playerName = player.getGameProfile().name();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        player.connection.disconnect(Component.literal("You are now AFK!"));
        addFakePlayer(level, x, y, z, playerName, yaw, pitch);
        return true;
    }

    public static void onPlayerJoin(@NotNull ServerPlayer player) {
        if (FakePlayer.isFake(player)) return;

        String playerName = player.getGameProfile().name();
        MinecraftServer server = player.level().getServer();

        for (ServerLevel level : server.getAllLevels()) {
            removeFakePlayer(level, null, playerName);
        }
    }

    public static boolean addFakePlayer(ServerLevel level, double x, double y, double z,
                                         String name, float yaw, float pitch) {
        var state = AFKAnchorsState.get(level);
        if (!state.add(x, y, z, name, yaw, pitch)) return false;

        MinecraftServer server = level.getServer();
        ServerPlayer fakePlayer = FakePlayer.spawn(server, level, x, y, z, name, yaw, pitch);
        activeFakePlayers.put(name, fakePlayer);
        return true;
    }

    public static boolean removeFakePlayer(ServerLevel level, BlockPos pos, String name) {
        var state = AFKAnchorsState.get(level);
        List<AFKAnchorsState.AFKAnchor> removed = state.removeAnchor(pos, name);
        if (removed.isEmpty()) return false;

        for (AFKAnchorsState.AFKAnchor a : removed) {
            ServerPlayer fakePlayer = activeFakePlayers.remove(a.name());
            if (fakePlayer != null) {
                FakePlayer.remove(level.getServer(), fakePlayer);
            }
        }
        return true;
    }

    public static @NotNull List<BlockPos> getAnchorPositions(ServerLevel level) {
        return AFKAnchorsState.get(level).getAllPositions();
    }

    public static void restoreFakePlayers(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            var entries = AFKAnchorsState.get(level).getAllEntries();
            for (AFKAnchorsState.AFKAnchor entry : entries) {
                ServerPlayer fakePlayer = FakePlayer.spawn(server, level,
                        entry.x(), entry.y(), entry.z(), entry.name(),
                        entry.yaw(), entry.pitch());
                activeFakePlayers.put(entry.name(), fakePlayer);
            }
        }
    }

    public static void clearFakePlayers() {
        activeFakePlayers.clear();
    }
}
