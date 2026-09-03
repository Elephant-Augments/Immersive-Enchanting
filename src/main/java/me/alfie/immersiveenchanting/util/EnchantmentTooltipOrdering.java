package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.api.filter.FilterBranches;
import me.alfie.immersiveenchanting.api.filter.IsolatedFilterBranch;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sorts enchantments on item tooltips by their isolated filter.
 */
public final class EnchantmentTooltipOrdering {

    private EnchantmentTooltipOrdering() {}

    public static int sortPriority(Holder<Enchantment> enchantment) {
        return FilterBranches.resolveIsolatedBranch(enchantment)
                .map(IsolatedFilterBranch::tooltipSortPriority)
                .orElse(0);
    }

    /**
     * @return {@code true} when at least two enchantments on the item have different filters.
     */
    public static boolean needsCustomOrder(ItemEnchantments enchantments) {
        if(enchantments == null || enchantments.isEmpty()) {
            return false;
        }

        Set<Integer> priorities = new HashSet<>();
        for(Holder<Enchantment> enchantment : enchantments.keySet()) {
            priorities.add(sortPriority(enchantment));
            if(priorities.size() > 1) {
                return true;
            }
        }
        return false;
    }

    public static List<Holder<Enchantment>> sortedKeys(ItemEnchantments enchantments) {
        List<Holder<Enchantment>> keys = new ArrayList<>(enchantments.keySet());
        keys.sort(Comparator
                .comparingInt(EnchantmentTooltipOrdering::sortPriority)
                .thenComparing(holder -> holder.unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse("")));
        return keys;
    }
}
