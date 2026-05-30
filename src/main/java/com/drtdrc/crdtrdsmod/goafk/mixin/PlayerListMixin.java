package com.drtdrc.crdtrdsmod.goafk.mixin;

import com.drtdrc.crdtrdsmod.goafk.DummyPlayerManager;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Redirect(method = "placeNewPlayer", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
    ))
    private void goafk_joinMessage(PlayerList instance, Component message, boolean overlay, @Local(argsOnly = true, name = "player") ServerPlayer player) {
        if (!DummyPlayerManager.isDummy(player)) {
            instance.broadcastSystemMessage(message, overlay);
        }
    }
}
