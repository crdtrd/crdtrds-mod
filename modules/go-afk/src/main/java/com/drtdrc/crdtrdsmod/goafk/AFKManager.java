package com.drtdrc.crdtrdsmod.goafk;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AFKManager {
    private AFKManager() {}

    public static final int TICKET_LEVEL_RADIUS = 3;

    public static String getDefaultName(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    public static void spawnAnchorLabel(ServerLevel level, BlockPos pos, String name) {
        Display.TextDisplay label = EntityType.TEXT_DISPLAY.create(level, EntitySpawnReason.CHUNK_GENERATION);
        if (label == null) return;

        label.snapTo(pos.getX() + 0.5, pos.getY() + 2.1, pos.getZ() + 0.5, 0f, 0f);
        label.setNoGravity(true);
        label.setSilent(true);
        label.setInvulnerable(true);
        label.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        label.setGlowingTag(true);
        label.setBackgroundColor(0x40000000);
        label.setLineWidth(140);
        label.setText(Component.literal(name).withStyle(ChatFormatting.WHITE));

        level.addFreshEntity(label);
    }

    private static void removeAnchorLabel(ServerLevel level, BlockPos pos, String name) {
        net.minecraft.world.phys.AABB search = new net.minecraft.world.phys.AABB(
                pos.getX() + 0.4, pos.getY(), pos.getZ() + 0.4,
                pos.getX() + 0.6, pos.getY() + 3.0, pos.getZ() + 0.6
        );
        for (Display.TextDisplay td : level.getEntitiesOfClass(Display.TextDisplay.class, search,
                tde -> tde.getText().getString().equals(name))) {
            td.discard();
        }
    }

    public static int computeRadius(@NotNull MinecraftServer server) {
        return Math.max(
                server.getPlayerList().getViewDistance(),
                server.getPlayerList().getSimulationDistance()
        );
    }

    public static void addTicketsAround(@NotNull ServerLevel level, BlockPos pos, int radius) {
        ServerChunkCache cm = level.getChunkSource();
        ChunkPos center = ChunkPos.containing(pos);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos cp = new ChunkPos(center.x() + dx, center.z() + dz);
                cm.addTicketWithRadius(TicketType.FORCED, cp, TICKET_LEVEL_RADIUS);
            }
        }
    }

    public static void removeTicketsAround(@NotNull ServerLevel level, BlockPos pos, int radius) {
        var state = AFKAnchorsState.get(level);
        ServerChunkCache cm = level.getChunkSource();

        Set<ChunkPos> keep = new HashSet<>();
        for (BlockPos p : state.getAllPositions()) {
            ChunkPos c = ChunkPos.containing(p);
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    keep.add(new ChunkPos(c.x() + dx, c.z() + dz));
                }
            }
        }

        ChunkPos center = ChunkPos.containing(pos);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                ChunkPos cp = new ChunkPos(center.x() + dx, center.z() + dz);
                if (!keep.contains(cp)) {
                    cm.removeTicketWithRadius(TicketType.FORCED, cp, TICKET_LEVEL_RADIUS);
                }
            }
        }
    }

    public static boolean goAFKAndKick(@NotNull ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = player.blockPosition();
        String playerName = player.getGameProfile().name();

        addAnchor(level, pos, playerName);
        player.connection.disconnect(Component.literal("You are now AFK!"));
        return true;
    }

    public static void onPlayerJoin(@NotNull ServerPlayer player) {
        String playerName = player.getGameProfile().name();
        BlockPos pos = player.blockPosition();
        ServerLevel level = (ServerLevel) player.level();
        removeAnchor(level, pos, playerName);
    }

    public static @NotNull List<BlockPos> getAnchorPositions(ServerLevel level) {
        return AFKAnchorsState.get(level).getAllPositions();
    }

    public static boolean addAnchor(ServerLevel level, BlockPos pos, String name) {
        var state = AFKAnchorsState.get(level);
        if (!state.add(pos, name)) return false;
        addTicketsAround(level, pos, computeRadius(level.getServer()));
        spawnAnchorLabel(level, pos, name);
        return true;
    }

    public static boolean removeAnchor(ServerLevel level, BlockPos pos, String name) {
        var anchorState = AFKAnchorsState.get(level);
        List<AFKAnchorsState.AFKAnchor> removed = anchorState.removeAnchor(pos, name);
        if (removed.isEmpty()) return false;
        for (AFKAnchorsState.AFKAnchor a : removed) {
            BlockPos p = a.pos();
            removeTicketsAround(level, p, computeRadius(level.getServer()));
            removeAnchorLabel(level, p, a.name());
        }
        return true;
    }
}
