package com.drtdrc.crdtrdsmod.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class PortalsUtil {

    private static final int MAX_SEARCH = 256;

    private PortalsUtil() {}

    public static void removeNearbyPortalBlocks(ServerLevel level, BlockPos origin) {
        removeNearbyBlocks(level, origin, Blocks.END_PORTAL);
        removeNearbyBlocks(level, origin, Blocks.NETHER_PORTAL);
    }

    private static void removeNearbyBlocks(ServerLevel level, BlockPos origin, net.minecraft.world.level.block.Block portalBlock) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        for (Direction d : Direction.values()) {
            BlockPos neighbor = origin.relative(d);
            if (level.getBlockState(neighbor).is(portalBlock)) {
                queue.add(neighbor);
            }
        }

        while (!queue.isEmpty() && visited.size() < MAX_SEARCH) {
            BlockPos pos = queue.poll();
            if (!visited.add(pos)) continue;
            BlockState state = level.getBlockState(pos);
            if (!state.is(portalBlock)) continue;
            level.removeBlock(pos, false);
            for (Direction d : Direction.values()) {
                BlockPos neighbor = pos.relative(d);
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
    }
}
