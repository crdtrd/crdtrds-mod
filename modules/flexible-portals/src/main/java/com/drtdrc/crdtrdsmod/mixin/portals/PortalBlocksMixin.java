package com.drtdrc.crdtrdsmod.mixin.portals;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(Blocks.class)
public class PortalBlocksMixin {

    @Inject(
            method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void crdtrdsmod$onRegisterPortalBlocks(ResourceKey<Block> key, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings, CallbackInfoReturnable<Block> cir) {
        if (!ModConfig.get().flexiblePortals) return;

        String id = key.identifier().toString();

        if (id.equals("minecraft:end_portal_frame")) {
            BlockBehaviour.Properties newSettings = BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .lightLevel(state -> 1)
                    .strength(50.0f, 3600000.0f)
                    .requiresCorrectToolForDrops();
            Block block = factory.apply(newSettings.setId(key));
            cir.setReturnValue(Registry.register(BuiltInRegistries.BLOCK, key, block));
        }

        if (id.equals("minecraft:end_portal")) {
            BlockBehaviour.Properties newSettings = BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollision()
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 15)
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK);
            Block block = factory.apply(newSettings.setId(key));
            cir.setReturnValue(Registry.register(BuiltInRegistries.BLOCK, key, block));
        }

        if (id.equals("minecraft:nether_portal")) {
            BlockBehaviour.Properties newSettings = BlockBehaviour.Properties.of()
                    .noCollision()
                    .randomTicks()
                    .strength(-1.0f, 3600000.0f)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 11)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK);
            Block block = factory.apply(newSettings.setId(key));
            cir.setReturnValue(Registry.register(BuiltInRegistries.BLOCK, key, block));
        }
    }
}
