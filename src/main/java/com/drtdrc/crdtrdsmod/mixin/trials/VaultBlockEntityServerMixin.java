package com.drtdrc.crdtrdsmod.mixin.trials;

import com.drtdrc.crdtrdsmod.ModConfig;
import com.drtdrc.crdtrdsmod.trials.VaultServerDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(VaultBlockEntity.Server.class)
public abstract class VaultBlockEntityServerMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private static void crdtrdsmod$onTick(ServerLevel level, BlockPos pos, BlockState state,
                                          VaultConfig config, VaultServerData serverData,
                                          VaultSharedData sharedData, CallbackInfo ci) {
        if (!ModConfig.get().mineableTrials) return;

        VaultServerDataAccessor accessor = (VaultServerDataAccessor) (Object) serverData;
        int gcTicks = accessor.crdtrdsmod$getGlobalCooldownTicks();
        Map<UUID, Integer> pcTickMap = accessor.crdtrdsmod$getCooldownMap();
        List<ServerPlayer> onlinePlayers = level.getPlayers(p -> true);

        if (gcTicks > 0) {
            onlinePlayers.forEach(p -> {
                if (!accessor.crdtrdsmod$hasRewardedPlayer(p)) {
                    serverData.addToRewardedPlayers(p);
                }
            });
            pcTickMap.replaceAll((u, t) -> gcTicks);

            accessor.crdtrdsmod$setGlobalCooldownTicks(gcTicks - 1);

            return;
        }

        if (!pcTickMap.isEmpty()) {
            Iterator<Map.Entry<UUID, Integer>> it = pcTickMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Integer> e = it.next();
                int next = e.getValue() - 1;

                if (next <= 0) {
                    it.remove();
                    accessor.crdtrdsmod$removePlayerFromRewardedPlayers(e.getKey());
                } else {
                    e.setValue(next);
                }
            }
        }
    }
}
