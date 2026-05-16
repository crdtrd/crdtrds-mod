package com.drtdrc.crdtrdsmod.delimitedanvils.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow @Final private DataSlot cost;

    @Unique private boolean spoofingCreative = false;
    @Unique private boolean wasCreative = false;

    public AnvilMenuMixin(MenuType<?> type, int syncId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(type, syncId, inventory, access, slotDefinition);
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40))
    private int crdtrdsmod$removeAnvilLimit(int original) {
        return Integer.MAX_VALUE;
    }

    @Inject(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL")
    )
    private void crdtrdsmod$afterConstructor(int containerId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        if (!(this.player instanceof ServerPlayer sp)) return;
        wasCreative = sp.isCreative();
        if (!wasCreative) {
            crdtrdsmod$sendCreativeSpoof(sp, true);
            spoofingCreative = true;
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$onMayPickup(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        // return (player.hasInfiniteMaterials() || player.experienceLevel >= this.cost.get()) && this.cost.get() > 0;
        // if not spoofing creative, then we are in creative, so we return true.
        // if we are spoofing creative, then we check if you can afford this. true if you can false if you can't
        if (spoofingCreative) {
            cir.setReturnValue((player.experienceLevel >= this.cost.get() && this.cost.get() > 0));
        }
        else {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void crdtrdsmod$onTakeXPCharge(Player player, int amount) {
        if (spoofingCreative) {
            player.giveExperienceLevels(-cost.get());
        }
    }

    @Override
    public void removed(@NonNull Player player) {
        if (player instanceof ServerPlayer sp && spoofingCreative && !wasCreative) {
            crdtrdsmod$sendCreativeSpoof(sp, false);
            spoofingCreative = false;
        }
        super.removed(player);
    }

    @Unique
    private void crdtrdsmod$sendCreativeSpoof(ServerPlayer sp, boolean pretendCreative) {
        Abilities src = sp.getAbilities();
        Abilities fake = new Abilities();
        fake.invulnerable = src.invulnerable;
        fake.mayfly = false;
        fake.instabuild = pretendCreative;
        fake.setFlyingSpeed(src.getFlyingSpeed());
        fake.setWalkingSpeed(src.getWalkingSpeed());
        sp.connection.send(new ClientboundPlayerAbilitiesPacket(fake));
    }
}
