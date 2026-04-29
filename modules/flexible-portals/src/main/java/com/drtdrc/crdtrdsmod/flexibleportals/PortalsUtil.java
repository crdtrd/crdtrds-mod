package com.drtdrc.crdtrdsmod.flexibleportals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PortalsUtil {

    private PortalsUtil() {}

    private static final int MAX_AREA = 4096;
    private static final int MAX_COMPONENT = 8192;
    private static final int MAX_SEARCH = 256;

    public enum Plane { HORIZONTAL, VERTICAL_X, VERTICAL_Z }

    public record FreeformRegion(Plane plane, List<BlockPos> interior) {
        public double centerX() { return interior.stream().mapToInt(BlockPos::getX).average().orElse(0) + 0.5; }
        public double centerY() { return interior.stream().mapToInt(BlockPos::getY).average().orElse(0) + 0.5; }
        public double centerZ() { return interior.stream().mapToInt(BlockPos::getZ).average().orElse(0) + 0.5; }
    }

    public record PortalSpec(
            List<Plane> allowedPlanes,
            Predicate<BlockState> frame,
            Predicate<BlockState> interior,
            Block portalBlock,
            Function<Plane, BlockState> orientedStateForPlane
    ) {
        public static PortalSpec end() {
            Predicate<BlockState> frame = s -> s.is(Blocks.END_PORTAL_FRAME) && s.getOptionalValue(BlockStateProperties.EYE).orElse(false);
            Predicate<BlockState> interior = s -> s.isAir() || s.is(Blocks.END_PORTAL);
            return new PortalSpec(
                    List.of(Plane.HORIZONTAL),
                    frame,
                    interior,
                    Blocks.END_PORTAL,
                    plane -> Blocks.END_PORTAL.defaultBlockState()
            );
        }

        public static PortalSpec nether() {
            Predicate<BlockState> frame = s -> s.is(Blocks.OBSIDIAN) || s.is(Blocks.CRYING_OBSIDIAN);
            Predicate<BlockState> interior = s -> s.isAir() || s.is(Blocks.NETHER_PORTAL) || s.is(BlockTags.FIRE);
            return new PortalSpec(
                    List.of(Plane.VERTICAL_X, Plane.VERTICAL_Z),
                    frame,
                    interior,
                    Blocks.NETHER_PORTAL,
                    plane -> {
                        var axis = (plane == Plane.VERTICAL_X) ? Direction.Axis.Z : Direction.Axis.X;
                        return Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
                    }
            );
        }
    }

    public static boolean findAndCreate(ServerLevel level, BlockPos origin, PortalSpec spec, SoundEvent creationSound) {
        var found = findBlocksToFill(level, origin, spec);
        if (found.isEmpty()) return false;

        var region = found.get();
        BlockState place = spec.orientedStateForPlane().apply(region.plane());

        for (BlockPos p : region.interior()) {
            BlockState s = level.getBlockState(p);
            if (spec.frame().test(s)) continue;
            if (spec.interior().test(s)) {
                if (!s.equals(place)) {
                    level.setBlock(p, place, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }

        if (creationSound != null) {
            float vol = Math.min(1.0f, 0.2f + region.interior().size() * 0.0025f);
            level.playSound(null,
                    BlockPos.containing(region.centerX(), region.centerY(), region.centerZ()),
                    creationSound, SoundSource.BLOCKS, vol, 1.0f);
        }
        return true;
    }

    public static Optional<FreeformRegion> findBlocksToFill(ServerLevel level, BlockPos origin, PortalSpec spec) {
        for (Plane plane : spec.allowedPlanes()) {
            BlockPos first = findNearestFrameOnPlane(level, origin, plane, spec.frame());
            if (first == null) continue;

            Set<Long> frameUV = collectFrameComponentUV(level, first, plane, spec.frame());
            if (frameUV.isEmpty()) continue;

            int constCoord = cOf(first, plane);
            Bounds b = boundsOf(frameUV);
            List<BlockPos> interior = interiorFromFrameUV(frameUV, plane, constCoord, b.minU, b.minV, b.maxU, b.maxV);

            boolean allClear = true;
            for (BlockPos p : interior) {
                BlockState s = level.getBlockState(p);
                if (!(spec.interior().test(s) || spec.frame().test(s))) {
                    allClear = false;
                    break;
                }
            }
            if (!allClear) continue;

            if (interior.isEmpty() || interior.size() > MAX_AREA) continue;

            return Optional.of(new FreeformRegion(plane, interior));
        }
        return Optional.empty();
    }

    // Geometry helpers (UV mapping on a given plane)
    private static int uOf(BlockPos p, Plane plane) { return switch (plane) { case HORIZONTAL -> p.getX(); case VERTICAL_X -> p.getZ(); case VERTICAL_Z -> p.getX(); }; }
    private static int vOf(BlockPos p, Plane plane) { return switch (plane) { case HORIZONTAL -> p.getZ(); case VERTICAL_X -> p.getY(); case VERTICAL_Z -> p.getY(); }; }
    private static int cOf(BlockPos p, Plane plane) { return switch (plane) { case HORIZONTAL -> p.getY(); case VERTICAL_X -> p.getX(); case VERTICAL_Z -> p.getZ(); }; }
    private static BlockPos fromUVC(int u, int v, int c, Plane plane) {
        return switch (plane) {
            case HORIZONTAL -> new BlockPos(u, c, v);
            case VERTICAL_X -> new BlockPos(c, v, u);
            case VERTICAL_Z -> new BlockPos(u, v, c);
        };
    }
    private static long pack(int u, int v) { return ((long) u << 32) ^ (v & 0xFFFFFFFFL); }

    private record Bounds(int minU, int minV, int maxU, int maxV) {}
    private static Bounds boundsOf(Set<Long> uv) {
        int minU = Integer.MAX_VALUE, minV = Integer.MAX_VALUE;
        int maxU = Integer.MIN_VALUE, maxV = Integer.MIN_VALUE;
        for (long k : uv) {
            int u = (int) (k >> 32), v = (int) k;
            if (u < minU) minU = u; if (u > maxU) maxU = u;
            if (v < minV) minV = v; if (v > maxV) maxV = v;
        }
        return new Bounds(minU, minV, maxU, maxV);
    }

    private static BlockPos findNearestFrameOnPlane(ServerLevel level, BlockPos origin, Plane plane, Predicate<BlockState> isFrame) {
        if (isFrame.test(level.getBlockState(origin))) return origin;

        for (int r = 1; r <= 24; r++) {
            switch (plane) {
                case HORIZONTAL -> {
                    int y = origin.getY();
                    for (int x = origin.getX() - r; x <= origin.getX() + r; x++) {
                        BlockPos p1 = new BlockPos(x, y, origin.getZ() - r), p2 = new BlockPos(x, y, origin.getZ() + r);
                        if (isFrame.test(level.getBlockState(p1))) return p1;
                        if (isFrame.test(level.getBlockState(p2))) return p2;
                    }
                    for (int z = origin.getZ() - r + 1; z <= origin.getZ() + r - 1; z++) {
                        BlockPos p1 = new BlockPos(origin.getX() - r, y, z), p2 = new BlockPos(origin.getX() + r, y, z);
                        if (isFrame.test(level.getBlockState(p1))) return p1;
                        if (isFrame.test(level.getBlockState(p2))) return p2;
                    }
                }
                case VERTICAL_X -> {
                    int x = origin.getX();
                    for (int y = origin.getY() - r; y <= origin.getY() + r; y++) {
                        BlockPos p1 = new BlockPos(x, y, origin.getZ() - r), p2 = new BlockPos(x, y, origin.getZ() + r);
                        if (isFrame.test(level.getBlockState(p1))) return p1;
                        if (isFrame.test(level.getBlockState(p2))) return p2;
                    }
                    for (int z = origin.getZ() - r + 1; z <= origin.getZ() + r - 1; z++) {
                        BlockPos p1 = new BlockPos(x, origin.getY() - r, z), p2 = new BlockPos(x, origin.getY() + r, z);
                        if (isFrame.test(level.getBlockState(p1))) return p1;
                        if (isFrame.test(level.getBlockState(p2))) return p2;
                    }
                }
                case VERTICAL_Z -> {
                    int z = origin.getZ();
                    for (int y = origin.getY() - r; y <= origin.getY() + r; y++) {
                        BlockPos p1 = new BlockPos(origin.getX() - r, y, z), p2 = new BlockPos(origin.getX() + r, y, z);
                        if (isFrame.test(level.getBlockState(p1))) return p1;
                        if (isFrame.test(level.getBlockState(p2))) return p2;
                    }
                    for (int x = origin.getX() - r + 1; x <= origin.getX() + r - 1; x++) {
                        BlockPos p1 = new BlockPos(x, origin.getY() - r, z), p2 = new BlockPos(x, origin.getY() + r, z);
                        if (isFrame.test(level.getBlockState(p1))) return p1;
                        if (isFrame.test(level.getBlockState(p2))) return p2;
                    }
                }
            }
        }
        return null;
    }

    private static final int[][] DIR8 = {
            {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}
    };

    private static Set<Long> collectFrameComponentUV(ServerLevel level, BlockPos seed, Plane plane, Predicate<BlockState> isFrame) {
        ArrayDeque<long[]> q = new ArrayDeque<>();
        HashSet<Long> seen = new HashSet<>();
        int C = cOf(seed, plane);
        int su = uOf(seed, plane), sv = vOf(seed, plane);
        long sk = pack(su, sv);
        q.add(new long[]{su, sv});
        seen.add(sk);

        while (!q.isEmpty()) {
            long[] cur = q.removeFirst();
            int u = (int) cur[0], v = (int) cur[1];

            for (int[] d : DIR8) {
                int nu = u + d[0], nv = v + d[1];
                long nk = pack(nu, nv);
                if (seen.contains(nk)) continue;
                BlockPos wp = fromUVC(nu, nv, C, plane);
                if (isFrame.test(level.getBlockState(wp))) {
                    seen.add(nk);
                    q.add(new long[]{nu, nv});
                    if (seen.size() > MAX_COMPONENT) return Set.of();
                }
            }
        }
        return seen;
    }

    private static final int[][] DIR4 = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private static List<BlockPos> interiorFromFrameUV(
            Set<Long> frameUV, Plane plane, int C,
            int minU, int minV, int maxU, int maxV) {

        int uMin = minU - 1, uMax = maxU + 1;
        int vMin = minV - 1, vMax = maxV + 1;

        HashSet<Long> outside = new HashSet<>();
        ArrayDeque<long[]> q = new ArrayDeque<>();

        for (int u = uMin; u <= uMax; u++) {
            enqueueIfFree(u, vMin, frameUV, outside, q);
            enqueueIfFree(u, vMax, frameUV, outside, q);
        }
        for (int v = vMin + 1; v <= vMax - 1; v++) {
            enqueueIfFree(uMin, v, frameUV, outside, q);
            enqueueIfFree(uMax, v, frameUV, outside, q);
        }

        while (!q.isEmpty()) {
            long[] cur = q.removeFirst();
            int u = (int) cur[0], v = (int) cur[1];
            for (int[] d : DIR4) {
                int nu = u + d[0], nv = v + d[1];
                if (nu < uMin || nu > uMax || nv < vMin || nv > vMax) continue;
                long nk = pack(nu, nv);
                if (outside.contains(nk)) continue;
                if (frameUV.contains(nk)) continue;
                outside.add(nk);
                q.addLast(new long[]{nu, nv});
            }
        }

        ArrayList<BlockPos> interior = new ArrayList<>();
        for (int u = minU; u <= maxU; u++) {
            for (int v = minV; v <= maxV; v++) {
                long k = pack(u, v);
                if (frameUV.contains(k)) continue;
                if (outside.contains(k)) continue;
                interior.add(fromUVC(u, v, C, plane));
                if (interior.size() > MAX_AREA) return List.of();
            }
        }
        return interior;
    }

    private static void enqueueIfFree(int u, int v, Set<Long> frameUV,
                                      Set<Long> outside, ArrayDeque<long[]> q) {
        long k = pack(u, v);
        if (!frameUV.contains(k) && outside.add(k)) q.add(new long[]{u, v});
    }

    public static void breakConnectedEndPortal(ServerLevel level, BlockPos start) {
        var q = new ArrayDeque<BlockPos>();
        var seen = new HashSet<BlockPos>();
        q.add(start);

        while (!q.isEmpty()) {
            BlockPos p = q.removeFirst();
            if (!seen.add(p)) continue;
            if (!level.getBlockState(p).is(Blocks.END_PORTAL)) continue;
            level.destroyBlock(p, false);
            for (Direction d : Direction.values()) q.add(p.relative(d));
        }
    }

    public static void breakConnectedNetherPortal(ServerLevel level, BlockPos start) {
        var q = new ArrayDeque<BlockPos>();
        var seen = new HashSet<BlockPos>();
        q.add(start);

        while (!q.isEmpty()) {
            BlockPos p = q.removeFirst();
            if (!seen.add(p)) continue;
            BlockState state = level.getBlockState(p);
            if (!state.is(Blocks.NETHER_PORTAL)) continue;
            level.destroyBlock(p, false);
            for (Direction d : Direction.values()) q.add(p.relative(d));
        }
    }

    public static void removeNearbyPortalBlocks(ServerLevel level, BlockPos origin) {
        removeNearbyBlocks(level, origin, Blocks.END_PORTAL);
        removeNearbyBlocks(level, origin, Blocks.NETHER_PORTAL);
    }

    private static void removeNearbyBlocks(ServerLevel level, BlockPos origin, Block portalBlock) {
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
