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

    @Unique private boolean wasCreative = false;
    @Unique private boolean currentlySpoofed = false;

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
            currentlySpoofed = true;
        }
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void crdtrdsmod$afterCreateResult(CallbackInfo ci) {
        if (!(this.player instanceof ServerPlayer sp) || wasCreative) return;
        int levelCost = this.cost.get();
        boolean canAfford = levelCost <= 0 || sp.experienceLevel >= levelCost;
        if (canAfford && !currentlySpoofed) {
            crdtrdsmod$sendCreativeSpoof(sp, true);
            currentlySpoofed = true;
        } else if (!canAfford && currentlySpoofed) {
            crdtrdsmod$sendCreativeSpoof(sp, false);
            currentlySpoofed = false;
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$onMayPickup(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (!player.isCreative()) {
            cir.setReturnValue(player.experienceLevel >= this.cost.get() && this.cost.get() > 0);
        }
    }

    @Redirect(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void crdtrdsmod$onTakeXPCharge(Player player, int amount) {
        if (!player.isCreative()) {
            player.giveExperienceLevels(-cost.get());
        }
    }

    @Override
    public void removed(@NonNull Player player) {
        if (player instanceof ServerPlayer sp && currentlySpoofed && !wasCreative) {
            crdtrdsmod$sendCreativeSpoof(sp, false);
            currentlySpoofed = false;
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
