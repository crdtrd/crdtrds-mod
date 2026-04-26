package com.drtdrc.crdtrdsmod.mixin.bedrock;

import com.drtdrc.crdtrdsmod.ModConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(Blocks.class)
public abstract class BedrockBlocksMixin {

    @Inject(
            method = "register(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void crdtrdsmod$makeBlocksMineable(ResourceKey<Block> key, BlockBehaviour.Properties properties, CallbackInfoReturnable<Block> cir) {
        String id = key.identifier().toString();

        if (ModConfig.get().mineableBedrock && id.equals("minecraft:bedrock")) {
            BlockBehaviour.Properties newProps = BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(100.0f, 3600000.0f)
                    .requiresCorrectToolForDrops();
            Block block = new Block(newProps.setId(key));
            cir.setReturnValue(Registry.register(BuiltInRegistries.BLOCK, key, block));
        }
    }

    @Inject(
            method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/util/function/Function;Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)Lnet/minecraft/world/level/block/Block;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void crdtrdsmod$makeTrialBlocksMineable(ResourceKey<Block> key, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties, CallbackInfoReturnable<Block> cir) {
        String id = key.identifier().toString();

        if (ModConfig.get().mineableTrials) {
            if (id.equals("minecraft:vault")) {
                BlockBehaviour.Properties newProps = BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .instrument(NoteBlockInstrument.BASEDRUM)
                        .noOcclusion()
                        .strength(50.0f)
                        .requiresCorrectToolForDrops();
                Block block = factory.apply(newProps.setId(key));
                cir.setReturnValue(Registry.register(BuiltInRegistries.BLOCK, key, block));
                return;
            }
            if (id.equals("minecraft:trial_spawner")) {
                BlockBehaviour.Properties newProps = BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .instrument(NoteBlockInstrument.BASEDRUM)
                        .noOcclusion()
                        .strength(50.0f)
                        .requiresCorrectToolForDrops();
                Block block = factory.apply(newProps.setId(key));
                cir.setReturnValue(Registry.register(BuiltInRegistries.BLOCK, key, block));
            }
        }
    }
}
