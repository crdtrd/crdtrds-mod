package com.drtdrc.crdtrdsmod.delimitedanvils.mixin;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class DelimitedAnvilMixin extends ItemCombinerMenu {

    @Shadow @Final private DataSlot cost;

    @Unique private boolean spoofingCreative = false;
    @Unique private boolean wasCreative = false;

    public DelimitedAnvilMixin(MenuType<?> type, int syncId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
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
    private void crdtrdsmod$afterConstructor(int syncId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        if (!(this.player instanceof ServerPlayer sp)) return;
        wasCreative = sp.isCreative();
        if (!wasCreative) {
            crdtrdsmod$sendCreativeSpoof(sp, true);
            spoofingCreative = true;
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void crdtrdsmod$onMayPickup(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (player.getAbilities().instabuild) return;
        int levelCost = this.cost.get();
        boolean ok = present && levelCost > 0 && player.experienceLevel >= levelCost;
        cir.setReturnValue(ok);
    }

    @Redirect(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperienceLevels(I)V")
    )
    private void crdtrdsmod$onTakeXPCharge(Player player, int ignoredDelta) {
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-cost.get());
        }
    }

    @Override
    public void removed(Player player) {
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
