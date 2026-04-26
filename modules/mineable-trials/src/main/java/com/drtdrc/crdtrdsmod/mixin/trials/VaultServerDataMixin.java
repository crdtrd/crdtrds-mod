package com.drtdrc.crdtrdsmod.mixin.trials;

import com.drtdrc.crdtrdsmod.trials.VaultServerDataAccessor;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(VaultServerData.class)
public abstract class VaultServerDataMixin implements VaultServerDataAccessor {

    @Unique private static final int COOLDOWN_TICKS = 72000; // 20 * 60 * 60, 1 hour
    @Unique private int globalCooldownTicks = 0;
    @Unique private final Map<UUID, Integer> playerCooldownTicks = new HashMap<>();

    @Shadow @Final private Set<UUID> rewardedPlayers;

    @Unique private static final String CODEC_GLOBAL = "global_cooldown";
    @Unique private static final String CODEC_LIST = "player_cooldowns";
    @Unique private static final String CODEC_PLAYER = "player";
    @Unique private static final String CODEC_TICKS = "ticks";

    @Shadow @Mutable static Codec<VaultServerData> CODEC;

    @Shadow abstract boolean hasRewardedPlayer(Player player);

    @Invoker("markChanged")
    abstract void crdtrdsmod$markChanged();

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void augmentCodec(CallbackInfo ci) {
        Codec<VaultServerData> base = CODEC;
        CODEC = Codec.of(
                new Encoder<VaultServerData>() {
                    @Override
                    public <T> DataResult<T> encode(VaultServerData input, DynamicOps<T> ops, T prefix) {
                        DataResult<T> encoded = base.encode(input, ops, prefix);
                        return encoded.flatMap(mapNode -> {
                            VaultServerDataAccessor accessor = (VaultServerDataAccessor) (Object) input;
                            DataResult<T> withGlobal = ops.mergeToMap(mapNode, ops.createString(CODEC_GLOBAL), ops.createInt(accessor.crdtrdsmod$getGlobalCooldownTicks()));
                            return withGlobal.flatMap(current -> {
                                List<T> listElems = new ArrayList<>();
                                for (Map.Entry<UUID, Integer> e : accessor.crdtrdsmod$getCooldownMap().entrySet()) {
                                    T entry = ops.createMap(Map.of(
                                            ops.createString(CODEC_PLAYER), ops.createString(e.getKey().toString()),
                                            ops.createString(CODEC_TICKS), ops.createInt(e.getValue())
                                    ));
                                    listElems.add(entry);
                                }
                                T listNode = ops.createList(listElems.stream());
                                return ops.mergeToMap(current, ops.createString(CODEC_LIST), listNode);
                            });
                        });
                    }
                },
                new Decoder<VaultServerData>() {
                    @Override
                    public <T> DataResult<Pair<VaultServerData, T>> decode(DynamicOps<T> ops, T input) {
                        DataResult<Pair<VaultServerData, T>> baseRes = base.decode(ops, input);
                        return baseRes.map(pair -> {
                            VaultServerData data = pair.getFirst();
                            com.mojang.serialization.Dynamic<T> dyn = new com.mojang.serialization.Dynamic<>(ops, input);

                            int global = dyn.get(CODEC_GLOBAL).asInt(0);

                            Map<UUID, Integer> map = new HashMap<>();
                            dyn.get(CODEC_LIST).asList(el -> {
                                String u = el.get(CODEC_PLAYER).asString(null);
                                int ticks = el.get(CODEC_TICKS).asInt(0);
                                if (u != null) {
                                    try { map.put(UUID.fromString(u), ticks); } catch (IllegalArgumentException ignored) {}
                                }
                                return el;
                            });
                            VaultServerDataAccessor accessor = (VaultServerDataAccessor) (Object) data;
                            accessor.crdtrdsmod$setGlobalCooldownTicks(global);
                            accessor.crdtrdsmod$setCooldownMap(map);

                            return Pair.of(data, pair.getSecond());
                        });
                    }
                }
        );
    }

    @Inject(method = "set", at = @At(value = "TAIL"))
    void onSet(VaultServerData data, CallbackInfo ci) {
        var sourceData = (VaultServerDataAccessor) (Object) data;
        crdtrdsmod$setGlobalCooldownTicks(sourceData.crdtrdsmod$getGlobalCooldownTicks());
        crdtrdsmod$setCooldownMap(sourceData.crdtrdsmod$getCooldownMap());
    }

    @Inject(method = "addToRewardedPlayers", at = @At(value = "HEAD"))
    void onAddToRewardedPlayers(Player player, CallbackInfo ci) {
        playerCooldownTicks.put(player.getUUID(), COOLDOWN_TICKS);
    }

    @Override
    public int crdtrdsmod$getPlayerCooldownTicks(Player player) {
        return (player == null) ? 0 : playerCooldownTicks.getOrDefault(player.getUUID(), 0);
    }

    @Override
    public void crdtrdsmod$setPlayerCooldownTicks(Player player, int ticks) {
        playerCooldownTicks.put(player.getUUID(), ticks);
        crdtrdsmod$markChanged();
    }

    @Override
    public int crdtrdsmod$getGlobalCooldownTicks() {
        return globalCooldownTicks;
    }

    @Override
    public void crdtrdsmod$setGlobalCooldownTicks(int ticks) {
        globalCooldownTicks = ticks;
        crdtrdsmod$markChanged();
    }

    @Override
    public void crdtrdsmod$removePlayerFromRewardedPlayers(Player player) {
        rewardedPlayers.remove(player.getUUID());
        crdtrdsmod$markChanged();
    }

    @Override
    public void crdtrdsmod$removePlayerFromRewardedPlayers(UUID uuid) {
        rewardedPlayers.remove(uuid);
        crdtrdsmod$markChanged();
    }

    @Override
    public void crdtrdsmod$addPlayerToRewardedPlayers(Player player) {
        rewardedPlayers.add(player.getUUID());
        crdtrdsmod$markChanged();
    }

    @Override
    public boolean crdtrdsmod$hasRewardedPlayer(Player player) {
        return this.hasRewardedPlayer(player);
    }

    @Override
    public Map<UUID, Integer> crdtrdsmod$getCooldownMap() {
        return playerCooldownTicks;
    }

    @Override
    public void crdtrdsmod$setCooldownMap(Map<UUID, Integer> map) {
        playerCooldownTicks.clear();
        if (map != null) playerCooldownTicks.putAll(map);
        crdtrdsmod$markChanged();
    }

    @Override
    public int crdtrdsmod$getCooldownTickConstant() {
        return COOLDOWN_TICKS;
    }
}
