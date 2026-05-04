package com.drtdrc.crdtrdsmod.enchantingencore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// for calculating enchantment selection weights when enchanting stuff, providing a bias towards one enchantment or another
public final class EnchantmentSelectionBiasContext {
    private EnchantmentSelectionBiasContext() {}

    private static final ThreadLocal<Map<Holder<Enchantment>, Integer>> BIAS =
            ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<Boolean> ACTIVE =
            ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static void compute(Level level, BlockPos tablePos) {
        Map<Holder<Enchantment>, Integer> map = BIAS.get();
        map.clear();
        ACTIVE.set(Boolean.TRUE);

        for (BlockPos off : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (!EnchantingTableBlock.isValidBookShelf(level, tablePos, off)) continue;

            BlockPos bp = tablePos.offset(off);
            BlockState st = level.getBlockState(bp);
            if (!st.is(Blocks.CHISELED_BOOKSHELF)) continue;

            BlockEntity be = level.getBlockEntity(bp);
            if (!(be instanceof ChiseledBookShelfBlockEntity shelf)) continue;

            int slots = shelf.getContainerSize();
            boolean allEnchanted = true;
            for (int i = 0; i < slots; i++) {
                ItemStack s = shelf.getItem(i);
                if (!s.is(Items.ENCHANTED_BOOK)) { allEnchanted = false; break; }
            }
            if (!allEnchanted) continue;

            Set<Holder<Enchantment>> common = null;
            int bookCount = 0;
            for (int i = 0; i < slots; i++) {
                ItemStack book = shelf.getItem(i);
                ItemEnchantments stored = book.get(DataComponents.STORED_ENCHANTMENTS);
                if (stored == null) { allEnchanted = false; break; }
                bookCount++;

                Set<Holder<Enchantment>> thisSet = new HashSet<>(stored.keySet());

                if (common == null) {
                    common = thisSet;
                } else {
                    common.retainAll(thisSet);
                    if (common.isEmpty()) break;
                }
            }

            if (!allEnchanted || bookCount == 0 || common == null || common.isEmpty()) continue;

            for (Holder<Enchantment> ench : common) {
                map.merge(ench, 1, Integer::sum);
            }
        }
    }

    public static void deactivate() {
        ACTIVE.set(Boolean.FALSE);
        BIAS.remove();
    }

    public static int bonus(Holder<Enchantment> ench) {
        return BIAS.get().getOrDefault(ench, 0);
    }
}
