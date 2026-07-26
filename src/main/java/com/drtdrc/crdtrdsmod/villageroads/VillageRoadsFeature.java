package com.drtdrc.crdtrdsmod.villageroads;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructureSets;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates roads connecting neighbouring villages using the village street palette.
 *
 * <p>Villages are placed on a deterministic {@link RandomSpreadStructurePlacement} grid, so every
 * chunk can independently recompute the same set of village anchor positions from the world seed
 * alone. Each village is connected to its single nearest neighbour, forming a deterministic
 * "nearest-neighbour graph" of road segments. For the chunk currently being decorated, any road
 * segment that crosses it is stamped column-by-column onto the surface (following the world-surface
 * heightmap), clipped to the chunk boundary. Because the graph is a pure function of the seed, roads
 * are continuous across chunk boundaries without any cross-chunk writes.
 */
public class VillageRoadsFeature extends Feature<NoneFeatureConfiguration> {

    /** How far (in grid cells) around the current chunk to look for road-segment endpoints. */
    private static final int SOURCE_CELL_RADIUS = 5;
    /** How far (in grid cells) around a village to search for its nearest neighbour. */
    private static final int NEIGHBOUR_CELL_RADIUS = 3;
    /** Half of the road width; distance-to-segment threshold in blocks. */
    private static final double ROAD_HALF_WIDTH = 1.6;
    /** Water spans deeper than this (in blocks) are not bridged; the road stops at the shore. */
    private static final int MAX_BRIDGE_DEPTH = 4;
    /**
     * How far (in blocks, measured along the road) a bridge may reach from dry land. Lake and ice
     * columns further than this from the nearest shore are left open, so the road only dips a little
     * way into lakes rather than crossing them, while narrow streams still bridge fully.
     */
    private static final int MAX_LAKE_PENETRATION = 5;
    /**
     * Each road segment is pulled back this many blocks from both village centres so the road begins
     * and ends at the fringe of a village rather than cutting through its buildings.
     */
    private static final int VILLAGE_EDGE_CLEARANCE = 48;
    /** Height (in blocks) above the path to clear tree trunks/canopy and other foliage. */
    private static final int MAX_TREE_CLEAR = 3;
    /** Radius (in chunks) around the current chunk to gather structure bounding boxes to avoid. */
    private static final int STRUCTURE_SCAN_RADIUS = 8;

    public VillageRoadsFeature(final Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(final FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkGenerator generator = context.chunkGenerator();
        BlockPos origin = context.origin();

        StructureSet villages = level.registryAccess()
                .lookupOrThrow(Registries.STRUCTURE_SET)
                .getValueOrThrow(BuiltinStructureSets.VILLAGES);
        StructurePlacement placement = villages.placement();
        if (!(placement instanceof RandomSpreadStructurePlacement spread)) {
            return false;
        }

        int spacing = spread.spacing();
        if (spacing <= 0) {
            return false;
        }
        long seed = level.getSeed();

        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        int minBlockX = origin.getX();
        int minBlockZ = origin.getZ();
        int maxBlockX = minBlockX + 15;
        int maxBlockZ = minBlockZ + 15;

        int cellX = Math.floorDiv(chunkX, spacing);
        int cellZ = Math.floorDiv(chunkZ, spacing);

        VillageLocator locator = new VillageLocator(spread, seed, spacing, level);
        int seaLevel = generator.getSeaLevel();
        // Structure footprints (villages, outposts, etc.) placed before this feature runs; road
        // columns falling inside any of them are skipped so roads never carve through structures.
        List<BoundingBox> structureBoxes = collectStructureBoxes(level, chunkX, chunkZ);
        boolean placedAny = false;

        for (int gx = cellX - SOURCE_CELL_RADIUS; gx <= cellX + SOURCE_CELL_RADIUS; gx++) {
            for (int gz = cellZ - SOURCE_CELL_RADIUS; gz <= cellZ + SOURCE_CELL_RADIUS; gz++) {
                ChunkPos a = locator.village(gx, gz);
                ChunkPos b = locator.nearestNeighbour(gx, gz);
                if (b == null) {
                    continue;
                }

                int ax = a.getMinBlockX() + 8;
                int az = a.getMinBlockZ() + 8;
                int bx = b.getMinBlockX() + 8;
                int bz = b.getMinBlockZ() + 8;

                // Pull the segment back from both village centres so it stops at the village edge.
                double dx = bx - ax;
                double dz = bz - az;
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len <= 2.0 * VILLAGE_EDGE_CLEARANCE) {
                    continue;
                }
                double ux = dx / len;
                double uz = dz / len;
                int sax = ax + (int) Math.round(ux * VILLAGE_EDGE_CLEARANCE);
                int saz = az + (int) Math.round(uz * VILLAGE_EDGE_CLEARANCE);
                int sbx = bx - (int) Math.round(ux * VILLAGE_EDGE_CLEARANCE);
                int sbz = bz - (int) Math.round(uz * VILLAGE_EDGE_CLEARANCE);

                if (!segmentIntersectsChunk(sax, saz, sbx, sbz, minBlockX, minBlockZ, maxBlockX, maxBlockZ)) {
                    continue;
                }

                placedAny |= stampSegment(level, seaLevel, sax, saz, sbx, sbz,
                        minBlockX, minBlockZ, maxBlockX, maxBlockZ, structureBoxes);
            }
        }

        return placedAny;
    }

    private static List<BoundingBox> collectStructureBoxes(final WorldGenLevel level,
                                                           final int chunkX, final int chunkZ) {
        List<BoundingBox> boxes = new ArrayList<>();
        for (int cx = chunkX - STRUCTURE_SCAN_RADIUS; cx <= chunkX + STRUCTURE_SCAN_RADIUS; cx++) {
            for (int cz = chunkZ - STRUCTURE_SCAN_RADIUS; cz <= chunkZ + STRUCTURE_SCAN_RADIUS; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                ChunkAccess chunk = level.getChunk(cx, cz);
                for (StructureStart start : chunk.getAllStarts().values()) {
                    if (start == null || !start.isValid()) {
                        continue;
                    }
                    for (StructurePiece piece : start.getPieces()) {
                        boxes.add(piece.getBoundingBox());
                    }
                }
            }
        }
        return boxes;
    }

    private static boolean insideStructure(final List<BoundingBox> boxes, final int wx, final int wz) {
        for (BoundingBox box : boxes) {
            if (box.intersects(wx, wz, wx, wz)) {
                return true;
            }
        }
        return false;
    }

    private boolean stampSegment(final WorldGenLevel level, final int seaLevel,
                                 final int ax, final int az, final int bx, final int bz,
                                 final int minBlockX, final int minBlockZ,
                                 final int maxBlockX, final int maxBlockZ,
                                 final List<BoundingBox> structureBoxes) {
        double dx = bx - ax;
        double dz = bz - az;
        double len = Math.sqrt(dx * dx + dz * dz);
        double ux = len == 0.0 ? 0.0 : dx / len;
        double uz = len == 0.0 ? 0.0 : dz / len;

        boolean placedAny = false;
        for (int wx = minBlockX; wx <= maxBlockX; wx++) {
            for (int wz = minBlockZ; wz <= maxBlockZ; wz++) {
                double dist = distanceToSegment(wx + 0.5, wz + 0.5, ax, az, bx, bz);
                if (dist <= ROAD_HALF_WIDTH && !insideStructure(structureBoxes, wx, wz)) {
                    placedAny |= placeRoadColumn(level, seaLevel, wx, wz, ux, uz);
                }
            }
        }
        return placedAny;
    }

    private boolean placeRoadColumn(final WorldGenLevel level, final int seaLevel,
                                    final int wx, final int wz, final double ux, final double uz) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, wx, wz);
        int topY = surfaceY - 1;
        if (topY <= level.getMinY() || surfaceY >= level.getMaxY()) {
            return false;
        }

        BlockPos topPos = new BlockPos(wx, topY, wz);
        BlockState topState = level.getBlockState(topPos);
        boolean water = !topState.getFluidState().isEmpty();
        boolean ice = isIce(topState);

        if (water || ice) {
            // Open water depth can be measured; ice hides its depth, so rely on the shore check for it.
            if (water) {
                int floorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, wx, wz);
                int depth = topY - floorY + 1;
                if (depth > MAX_BRIDGE_DEPTH) {
                    return false;
                }
            }
            if (!nearShore(level, wx, wz, ux, uz)) {
                return false;
            }
            int bridgeY = Math.max(topY, seaLevel - 1);
            Holder<Biome> biome = level.getBiome(topPos);
            level.setBlock(new BlockPos(wx, bridgeY, wz), bridgeBlock(biome).defaultBlockState(), 2);
            clearAbove(level, wx, bridgeY, wz);
            return true;
        }

        // Support the path so it does not float over an overhang or cave lip.
        BlockPos belowPos = topPos.below();
        if (level.getBlockState(belowPos).isAir() || !level.getFluidState(belowPos).isEmpty()) {
            level.setBlock(belowPos, Blocks.DIRT.defaultBlockState(), 2);
        }

        Holder<Biome> biome = level.getBiome(topPos);
        level.setBlock(topPos, surfaceBlock(biome, wx, wz), 2);
        clearAbove(level, wx, topY, wz);
        return true;
    }

    /**
     * True if dry land lies within {@link #MAX_LAKE_PENETRATION} blocks of this column measured along
     * the road direction (in either direction). Keeps bridges to the fringes of lakes.
     */
    private static boolean nearShore(final WorldGenLevel level, final int wx, final int wz,
                                     final double ux, final double uz) {
        if (ux == 0.0 && uz == 0.0) {
            return true;
        }
        for (int k = 1; k <= MAX_LAKE_PENETRATION; k++) {
            int fx = wx + (int) Math.round(ux * k);
            int fz = wz + (int) Math.round(uz * k);
            int bx = wx - (int) Math.round(ux * k);
            int bz = wz - (int) Math.round(uz * k);
            if (isLandColumn(level, fx, fz) || isLandColumn(level, bx, bz)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLandColumn(final WorldGenLevel level, final int x, final int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.getFluidState().isEmpty() && !isIce(state);
    }

    private static boolean isIce(final BlockState state) {
        return state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE);
    }

    /**
     * Clears the column above the road: removes tree trunks/canopy ({@link BlockTags#LOGS},
     * {@link BlockTags#LEAVES}) and any non-solid foliage (grass, flowers, snow layers, vines).
     * Stops at the first solid non-tree block so it never tunnels through terrain or structures.
     */
    private static void clearAbove(final WorldGenLevel level, final int wx, final int y, final int wz) {
        for (int oy = 1; oy <= MAX_TREE_CLEAR; oy++) {
            BlockPos pos = new BlockPos(wx, y + oy, wz);
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || !state.blocksMotion()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            } else {
                break;
            }
        }
    }

    private static BlockState surfaceBlock(final Holder<Biome> biome, final int wx, final int wz) {
        int r = deterministic(wx, wz) % 100;
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            return (r < 55 ? Blocks.RED_SANDSTONE : r < 80 ? Blocks.SMOOTH_RED_SANDSTONE
                    : r < 92 ? Blocks.CUT_RED_SANDSTONE : Blocks.TERRACOTTA).defaultBlockState();
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_DESERT)) {
            return (r < 45 ? Blocks.SMOOTH_SANDSTONE : r < 65 ? Blocks.CUT_SANDSTONE
                    : r < 80 ? Blocks.SANDSTONE : Blocks.TERRACOTTA).defaultBlockState();
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_SNOWY)) {
            return (r < 55 ? Blocks.SNOW_BLOCK : r < 85 ? Blocks.DIRT_PATH : Blocks.COBBLESTONE)
                    .defaultBlockState();
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_TAIGA) || biome.is(BiomeTags.IS_TAIGA)) {
            return (r < 45 ? Blocks.DIRT_PATH : r < 75 ? Blocks.PODZOL
                    : r < 90 ? Blocks.COBBLESTONE : Blocks.MOSSY_COBBLESTONE).defaultBlockState();
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_SAVANNA) || biome.is(BiomeTags.IS_SAVANNA)) {
            return (r < 60 ? Blocks.DIRT_PATH : r < 85 ? Blocks.COARSE_DIRT : Blocks.GRAVEL)
                    .defaultBlockState();
        }
        // Plains and everything else.
        return (r < 70 ? Blocks.DIRT_PATH : r < 90 ? Blocks.GRAVEL : Blocks.COBBLESTONE)
                .defaultBlockState();
    }

    private static Block bridgeBlock(final Holder<Biome> biome) {
        if (biome.is(BiomeTags.HAS_VILLAGE_SNOWY) || biome.is(BiomeTags.HAS_VILLAGE_TAIGA)
                || biome.is(BiomeTags.IS_TAIGA)) {
            return Blocks.SPRUCE_PLANKS;
        }
        if (biome.is(BiomeTags.HAS_VILLAGE_SAVANNA) || biome.is(BiomeTags.IS_SAVANNA)) {
            return Blocks.ACACIA_PLANKS;
        }
        return Blocks.OAK_PLANKS;
    }

    private static int deterministic(final int x, final int z) {
        long h = (long) x * 0x2545F4914F6CDD1DL ^ (long) z * 0x9E3779B97F4A7C15L;
        h ^= (h >>> 29);
        h *= 0xBF58476D1CE4E5B9L;
        h ^= (h >>> 32);
        return (int) (h & 0x7FFFFFFFL);
    }

    private static boolean segmentIntersectsChunk(final int ax, final int az, final int bx, final int bz,
                                                  final int minX, final int minZ, final int maxX, final int maxZ) {
        int pad = (int) Math.ceil(ROAD_HALF_WIDTH) + 1;
        int segMinX = Math.min(ax, bx) - pad;
        int segMaxX = Math.max(ax, bx) + pad;
        int segMinZ = Math.min(az, bz) - pad;
        int segMaxZ = Math.max(az, bz) + pad;
        return segMaxX >= minX && segMinX <= maxX && segMaxZ >= minZ && segMinZ <= maxZ;
    }

    private static double distanceToSegment(final double px, final double pz,
                                            final double ax, final double az,
                                            final double bx, final double bz) {
        double dx = bx - ax;
        double dz = bz - az;
        double lenSq = dx * dx + dz * dz;
        if (lenSq == 0.0) {
            double ddx = px - ax;
            double ddz = pz - az;
            return Math.sqrt(ddx * ddx + ddz * ddz);
        }
        double t = ((px - ax) * dx + (pz - az) * dz) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        double cx = ax + t * dx;
        double cz = az + t * dz;
        double ddx = px - cx;
        double ddz = pz - cz;
        return Math.sqrt(ddx * ddx + ddz * ddz);
    }

    /**
     * Deterministically maps village grid cells to their anchor chunks and resolves each village's
     * nearest neighbour. All lookups depend only on the seed and grid coordinates, so every chunk
     * that considers a given edge derives the identical result.
     */
    private static final class VillageLocator {
        /** Y (in biome-quart coordinates, y=64) at which to sample the biome for village viability. */
        private static final int BIOME_SAMPLE_QUART_Y = 16;

        // Village positions and viability are pure functions of the world seed, so they are cached
        // statically across chunks. The caches are cleared whenever the observed seed changes.
        private static final Map<Long, ChunkPos> POS_CACHE = new ConcurrentHashMap<>();
        private static final Map<Long, Boolean> VILLAGE_CACHE = new ConcurrentHashMap<>();
        private static long cachedSeed;
        private static boolean seedLoaded;

        private final RandomSpreadStructurePlacement spread;
        private final long seed;
        private final int spacing;
        private final WorldGenLevel level;

        private VillageLocator(final RandomSpreadStructurePlacement spread, final long seed,
                              final int spacing, final WorldGenLevel level) {
            this.spread = spread;
            this.seed = seed;
            this.spacing = spacing;
            this.level = level;
            ensureSeed(seed);
        }

        private static synchronized void ensureSeed(final long seed) {
            if (!seedLoaded || cachedSeed != seed) {
                POS_CACHE.clear();
                VILLAGE_CACHE.clear();
                cachedSeed = seed;
                seedLoaded = true;
            }
        }

        private static long cellKey(final int cellX, final int cellZ) {
            return (((long) cellX) << 32) ^ (cellZ & 0xFFFFFFFFL);
        }

        private ChunkPos village(final int cellX, final int cellZ) {
            return POS_CACHE.computeIfAbsent(cellKey(cellX, cellZ),
                    k -> spread.getPotentialStructureChunk(seed, cellX * spacing, cellZ * spacing));
        }

        /**
         * Whether a grid cell's candidate chunk lies in a biome that actually spawns villages. Cells
         * over ocean, forest, jungle, etc. return false, so roads never run to an empty candidate
         * spot and every real village still anchors a road.
         */
        private boolean hasVillage(final int cellX, final int cellZ) {
            return VILLAGE_CACHE.computeIfAbsent(cellKey(cellX, cellZ), k -> {
                ChunkPos pos = village(cellX, cellZ);
                Holder<Biome> biome = level.getUncachedNoiseBiome(
                        (pos.getMinBlockX() + 8) >> 2, BIOME_SAMPLE_QUART_Y, (pos.getMinBlockZ() + 8) >> 2);
                return biome.is(BiomeTags.HAS_VILLAGE_PLAINS)
                        || biome.is(BiomeTags.HAS_VILLAGE_DESERT)
                        || biome.is(BiomeTags.HAS_VILLAGE_SAVANNA)
                        || biome.is(BiomeTags.HAS_VILLAGE_SNOWY)
                        || biome.is(BiomeTags.HAS_VILLAGE_TAIGA);
            });
        }

        private ChunkPos nearestNeighbour(final int cellX, final int cellZ) {
            if (!hasVillage(cellX, cellZ)) {
                return null;
            }
            ChunkPos self = village(cellX, cellZ);
            ChunkPos best = null;
            long bestDist = Long.MAX_VALUE;
            for (int gx = cellX - NEIGHBOUR_CELL_RADIUS; gx <= cellX + NEIGHBOUR_CELL_RADIUS; gx++) {
                for (int gz = cellZ - NEIGHBOUR_CELL_RADIUS; gz <= cellZ + NEIGHBOUR_CELL_RADIUS; gz++) {
                    if ((gx == cellX && gz == cellZ) || !hasVillage(gx, gz)) {
                        continue;
                    }
                    ChunkPos other = village(gx, gz);
                    long dx = (long) other.getMinBlockX() - self.getMinBlockX();
                    long dz = (long) other.getMinBlockZ() - self.getMinBlockZ();
                    long distSq = dx * dx + dz * dz;
                    if (distSq < bestDist || (distSq == bestDist && isBefore(other, best))) {
                        bestDist = distSq;
                        best = other;
                    }
                }
            }
            return best;
        }

        private static boolean isBefore(final ChunkPos candidate, final ChunkPos current) {
            if (current == null) {
                return true;
            }
            if (candidate.getMinBlockX() != current.getMinBlockX()) {
                return candidate.getMinBlockX() < current.getMinBlockX();
            }
            return candidate.getMinBlockZ() < current.getMinBlockZ();
        }
    }
}
