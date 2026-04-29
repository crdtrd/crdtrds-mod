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

import java.util.*;

public final class AFKAnchorsState extends SavedData {

    public record AFKAnchor(BlockPos pos, String name) {
        public static final Codec<AFKAnchor> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(AFKAnchor::pos),
                        Codec.STRING.fieldOf("name").forGetter(AFKAnchor::name)
                ).apply(inst, AFKAnchor::new)
        );
    }

    public static final Codec<AFKAnchorsState> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    AFKAnchor.CODEC.listOf().optionalFieldOf("anchors", List.of()).forGetter(s -> s.afkAnchors)
            ).apply(inst, AFKAnchorsState::new)
    );

    public static final SavedDataType<AFKAnchorsState> TYPE =
            new SavedDataType<>(Identifier.fromNamespaceAndPath("crdtrdsmod", "afk_anchors"), AFKAnchorsState::new, CODEC, DataFixTypes.SAVED_DATA_MAP_DATA);

    private final ObjectArrayList<AFKAnchor> afkAnchors;

    public AFKAnchorsState() {
        this.afkAnchors = new ObjectArrayList<>();
    }

    private AFKAnchorsState(List<AFKAnchor> loaded) {
        this.afkAnchors = new ObjectArrayList<>(loaded);
    }

    public static AFKAnchorsState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public List<AFKAnchor> getAllEntries() {
        return Collections.unmodifiableList(afkAnchors);
    }

    public List<BlockPos> getAllPositions() {
        return afkAnchors.stream().map(AFKAnchor::pos).toList();
    }

    public boolean add(BlockPos pos, String name) {
        boolean exists = afkAnchors.stream().anyMatch(e -> e.pos.equals(pos) && e.name.equals(name));
        if (exists) return false;
        afkAnchors.add(new AFKAnchor(pos.immutable(), name));
        this.setDirty();
        return true;
    }

    public List<AFKAnchor> removeAnchor(BlockPos pos, String name) {
        final boolean hasPos = pos != null;
        List<AFKAnchor> removed = new ArrayList<>();
        Iterator<AFKAnchor> it = afkAnchors.iterator();
        while (it.hasNext()) {
            AFKAnchor a = it.next();
            boolean matches;
            if (hasPos) {
                matches = a.pos.equals(pos) && a.name.equals(name);
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
