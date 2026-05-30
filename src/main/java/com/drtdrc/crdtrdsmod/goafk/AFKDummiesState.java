package com.drtdrc.crdtrdsmod.goafk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class AFKDummiesState extends SavedData {

    public record AFKDummy(double x, double y, double z, String name, float yaw, float pitch) {
        public static final Codec<AFKDummy> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.DOUBLE.fieldOf("x").forGetter(AFKDummy::x),
                        Codec.DOUBLE.fieldOf("y").forGetter(AFKDummy::y),
                        Codec.DOUBLE.fieldOf("z").forGetter(AFKDummy::z),
                        Codec.STRING.fieldOf("name").forGetter(AFKDummy::name),
                        Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(AFKDummy::yaw),
                        Codec.FLOAT.optionalFieldOf("pitch", 0f).forGetter(AFKDummy::pitch)
                ).apply(inst, AFKDummy::new)
        );

        public BlockPos blockPos() {
            return BlockPos.containing(x, y, z);
        }
    }

    public static final Codec<AFKDummiesState> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    AFKDummy.CODEC.listOf().optionalFieldOf("dummies", List.of()).forGetter(s -> s.afkDummies)
            ).apply(inst, AFKDummiesState::new)
    );

    public static final SavedDataType<AFKDummiesState> TYPE =
            new SavedDataType<>(Identifier.fromNamespaceAndPath("crdtrdsmod", "afk_dummies"), AFKDummiesState::new, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA);

    private final ObjectArrayList<AFKDummy> afkDummies;

    public AFKDummiesState() {
        this.afkDummies = new ObjectArrayList<>();
    }

    private AFKDummiesState(List<AFKDummy> loaded) {
        this.afkDummies = new ObjectArrayList<>(loaded);
    }

    public static AFKDummiesState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public List<AFKDummy> getAllEntries() {
        return Collections.unmodifiableList(afkDummies);
    }

    public List<BlockPos> getAllPositions() {
        return afkDummies.stream().map(AFKDummy::blockPos).toList();
    }

    public boolean add(double x, double y, double z, String name, float yaw, float pitch) {
        boolean exists = afkDummies.stream().anyMatch(e -> e.name.equals(name));
        if (exists) return false;
        afkDummies.add(new AFKDummy(x, y, z, name, yaw, pitch));
        this.setDirty();
        return true;
    }

    public List<AFKDummy> removeDummy(BlockPos pos, String name) {
        final boolean hasPos = pos != null;
        List<AFKDummy> removed = new ArrayList<>();
        Iterator<AFKDummy> it = afkDummies.iterator();
        while (it.hasNext()) {
            AFKDummy a = it.next();
            boolean matches;
            if (hasPos) {
                matches = a.blockPos().equals(pos) && a.name.equals(name);
            } else {
                matches = a.name.equals(name);
            }
            if (matches) {
                removed.add(a);
                it.remove();
            }
        }
        if (!removed.isEmpty()) this.setDirty();
        return removed;
    }
}
