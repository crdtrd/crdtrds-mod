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

    private static final Map<String, ServerPlayer> activeDummyPlayers = new HashMap<>();

    public static String getDefaultName(BlockPos pos) {
        return "Dummy " ;
    }

    public static boolean goAFKAndKick(@NotNull ServerPlayer player) {
        ServerLevel level = player.level();
        String playerName = player.getGameProfile().name();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        DummyPlayerManager.GOING_AFK.add(player.getUUID());

        player.connection.disconnect(Component.literal("You are now AFK!"));
        addFakePlayer(level, x, y, z, playerName, yaw, pitch);
        return true;
    }

    public static void onPlayerJoin(@NotNull ServerPlayer player) {
        if (DummyPlayerManager.isDummy(player)) return;

        String playerName = player.getGameProfile().name();
        MinecraftServer server = player.level().getServer();

        for (ServerLevel level : server.getAllLevels()) {
            removeFakePlayer(level, null, playerName);
        }
    }

    public static boolean addFakePlayer(ServerLevel level, double x, double y, double z,
                                         String name, float yaw, float pitch) {

        var state = AFKDummiesState.get(level);

        String trimmedName = name.substring(0, Math.min(name.length(), 15));

        if (!state.add(x, y, z, trimmedName, yaw, pitch)) return false;

        MinecraftServer server = level.getServer();
        ServerPlayer fakePlayer = DummyPlayerManager.spawn(server, level, x, y, z, trimmedName, yaw, pitch);
        activeDummyPlayers.put(trimmedName, fakePlayer);
        return true;
    }

    public static boolean removeFakePlayer(ServerLevel level, BlockPos pos, String name) {
        var state = AFKDummiesState.get(level);
        List<AFKDummiesState.AFKDummy> removed = state.removeDummy(pos, name);
        if (removed.isEmpty()) return false;

        for (AFKDummiesState.AFKDummy a : removed) {
            ServerPlayer fakePlayer = activeDummyPlayers.remove(a.name());
            if (fakePlayer != null) {
                DummyPlayerManager.remove(level.getServer(), fakePlayer);
            }
        }
        return true;
    }

    public static @NotNull List<BlockPos> getAnchorPositions(ServerLevel level) {
        return AFKDummiesState.get(level).getAllPositions();
    }

    public static void restoreFakePlayers(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            var entries = AFKDummiesState.get(level).getAllEntries();
            for (AFKDummiesState.AFKDummy entry : entries) {
                ServerPlayer fakePlayer = DummyPlayerManager.spawn(server, level,
                        entry.x(), entry.y(), entry.z(), entry.name(),
                        entry.yaw(), entry.pitch());
                activeDummyPlayers.put(entry.name(), fakePlayer);
            }
        }
    }

    public static void clearFakePlayers() {
        activeDummyPlayers.clear();
    }
}
