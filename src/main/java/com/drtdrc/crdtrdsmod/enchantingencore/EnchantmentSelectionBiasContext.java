package com.drtdrc.crdtrdsmod.enchantingencore;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class EnchantmentSelectionBiasContext {
    private EnchantmentSelectionBiasContext() {}

    // maps each enchantment found in chiseled bookshelf books to the sum of its power levels
    private static final ThreadLocal<Map<Holder<Enchantment>, Integer>> WEIGHTS =
            ThreadLocal.withInitial(HashMap::new);

    // whether any valid chiseled bookshelves (full of enchanted books) were found
    private static final ThreadLocal<Boolean> HAS_CHISELED_BOOKSHELVES =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    private static final ThreadLocal<Boolean> ACTIVE =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static void compute(Level level, BlockPos tablePos) {
        Map<Holder<Enchantment>, Integer> map = WEIGHTS.get();
        map.clear();
        HAS_CHISELED_BOOKSHELVES.set(Boolean.FALSE);
        ACTIVE.set(Boolean.TRUE);

        for (BlockPos off : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (!EnchantingTableBlock.isValidBookShelf(level, tablePos, off)) continue;
            // do chiseled bookshelf things
            BlockPos bp = tablePos.offset(off);
            BlockState st = level.getBlockState(bp);
            if (!st.is(Blocks.CHISELED_BOOKSHELF)) continue;
            BlockEntity be = level.getBlockEntity(bp);
            if (!(be instanceof ChiseledBookShelfBlockEntity shelf)) continue;
            HAS_CHISELED_BOOKSHELVES.set(Boolean.TRUE);
            int slots = shelf.getContainerSize();
            // sum up power levels for each enchantment across all books in this shelf
            for (int i = 0; i < slots; i++) {
                ItemStack book = shelf.getItem(i);
                ItemEnchantments stored = book.get(DataComponents.STORED_ENCHANTMENTS);
                if (stored == null) continue;

                for (Object2IntMap.Entry<Holder<Enchantment>> entry : stored.entrySet()) {
                    map.merge(entry.getKey(), entry.getIntValue(), Integer::sum);
                }
            }
        }
    }

    public static void deactivate() {
        ACTIVE.set(Boolean.FALSE);
        HAS_CHISELED_BOOKSHELVES.set(Boolean.FALSE);
        WEIGHTS.remove();
    }

    /** Whether any valid chiseled bookshelves were found around the enchanting table. */
    public static boolean hasChiseledBookshelves() {
        return HAS_CHISELED_BOOKSHELVES.get();
    }

    /** The set of enchantments available from the chiseled bookshelves. */
    public static Set<Holder<Enchantment>> allowedEnchantments() {
        return WEIGHTS.get().keySet();
    }

    /** The summed power level for a given enchantment across all chiseled bookshelf books. */
    public static int weight(Holder<Enchantment> ench) {
        return WEIGHTS.get().getOrDefault(ench, 0);
    }

}
