package com.drtdrc.crdtrdsmod.mineabletrials;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;

public interface VaultServerDataAccessor {
    int crdtrdsmod$getPlayerCooldownTicks(Player player);
    void crdtrdsmod$setPlayerCooldownTicks(Player player, int ticks);
    int crdtrdsmod$getGlobalCooldownTicks();
    void crdtrdsmod$setGlobalCooldownTicks(int ticks);
    void crdtrdsmod$removePlayerFromRewardedPlayers(Player player);
    void crdtrdsmod$removePlayerFromRewardedPlayers(UUID uuid);
    void crdtrdsmod$addPlayerToRewardedPlayers(Player player);
    boolean crdtrdsmod$hasRewardedPlayer(Player player);
    Map<UUID, Integer> crdtrdsmod$getCooldownMap();
    void crdtrdsmod$setCooldownMap(Map<UUID, Integer> map);
    int crdtrdsmod$getCooldownTickConstant();
}
