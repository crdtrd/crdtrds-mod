package com.drtdrc.crdtrdsmod.goafk.mixin;

import com.drtdrc.crdtrdsmod.goafk.DummyPlayerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Redirect(method = "removePlayerFromWorld", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
    ))
    private void goafk_leaveMessage(PlayerList instance, Component message, boolean overlay) {
        if (DummyPlayerManager.isDummy(this.player)) {
            // dummy — suppress entirely
        } else if (DummyPlayerManager.GOING_AFK.remove(this.player.getUUID())) {
            // real player going AFK — custom message
            instance.broadcastSystemMessage(
                    Component.literal(this.player.getGameProfile().name() + " is now AFK")
                            .withStyle(s -> s.withItalic(true).withColor(0x888888)),
                    overlay
            );
        } else {
            // normal disconnect — leave untouched
            instance.broadcastSystemMessage(message, overlay);
        }
    }
}
