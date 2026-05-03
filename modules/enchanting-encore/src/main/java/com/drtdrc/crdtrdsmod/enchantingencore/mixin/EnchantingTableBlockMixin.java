package com.drtdrc.crdtrdsmod.enchantingencore.mixin;

import com.drtdrc.crdtrdsmod.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin extends BaseEntityBlock {

    @Shadow
    @Final
    @Mutable
    public static List<BlockPos> BOOKSHELF_OFFSETS;

    protected EnchantingTableBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void crdtrdsmod$tweakBookshelfOffsets(CallbackInfo ci) {
        if (!ModConfig.active().enchantingEncore) return;
        BOOKSHELF_OFFSETS = BlockPos.betweenClosedStream(-4, -4, -4, 4, 4, 4)
                .filter(pos -> Math.abs(pos.getX()) > 1 || Math.abs(pos.getZ()) > 1)
                .map(BlockPos::immutable)
                .toList();
    }

    @Inject(method = "isValidBookShelf", at = @At("HEAD"), cancellable = true)
    private static void crdtrdsmod$modifiedValidBookShelf(Level level, BlockPos tablePos, BlockPos providerOffset, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.active().enchantingEncore) return;

        BlockPos providerPos = tablePos.offset(providerOffset);
        BlockState provider = level.getBlockState(providerPos);

        boolean isProvider = provider.is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                || (provider.is(Blocks.CHISELED_BOOKSHELF) && isChiseledBookshelfFull(level, providerPos));

        BlockState transmitter = level.getBlockState(tablePos.offset(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2));
        boolean canTransmit = transmitter.is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)
                || transmitter.is(BlockTags.ENCHANTMENT_POWER_PROVIDER)
                || transmitter.is(Blocks.CHISELED_BOOKSHELF);

        cir.setReturnValue(isProvider && canTransmit);
    }

    @Unique
    private static boolean isChiseledBookshelfFull(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChiseledBookShelfBlockEntity shelf) {
            for (int i = 0; i < shelf.getContainerSize(); i++) {
                if (shelf.getItem(i).isEmpty()) return false;
            }
            return true;
        }
        return false;
    }

    @Inject(method = "getTicker", at = @At("RETURN"), cancellable = true)
    private <T extends BlockEntity> void crdtrdsmod$onGetTicker(Level level, BlockState state, BlockEntityType<T> type, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (level.isClientSide()) return;
        if (type != BlockEntityType.ENCHANTING_TABLE) return;

        BlockEntityTicker<T> existing = cir.getReturnValue();

        BlockEntityTicker<T> serverTicker = (w, pos, s, be) -> {
            if (!(w instanceof ServerLevel sw)) return;
            RandomSource rand = sw.getRandom();
            if (rand.nextInt(3) != 0) return;
            spawnEnchantParticlesServer(sw, pos, rand);
        };

        if (existing != null) {
            BlockEntityTicker<T> chained = (w2, p2, s2, be2) -> {
                existing.tick(w2, p2, s2, be2);
                serverTicker.tick(w2, p2, s2, be2);
            };
            cir.setReturnValue(chained);
        } else {
            cir.setReturnValue(serverTicker);
        }
    }

    @Unique
    private static void spawnEnchantParticlesServer(ServerLevel level, BlockPos tablePos, RandomSource random) {
        final double ox = tablePos.getX() + 0.5;
        final double oy = tablePos.getY() + 2.0;
        final double oz = tablePos.getZ() + 0.5;

        for (BlockPos offset : BOOKSHELF_OFFSETS) {
            if (random.nextInt(16) != 0) continue;
            if (!EnchantingTableBlock.isValidBookShelf(level, tablePos, offset)) continue;

            double tx = offset.getX() + random.nextFloat() - 0.5;
            double ty = offset.getY() - random.nextFloat() - 1.0f;
            double tz = offset.getZ() + random.nextFloat() - 0.5;

            level.sendParticles(ParticleTypes.ENCHANT, ox, oy, oz, 0, tx, ty, tz, 1.0);
        }
    }
}
