package me.alfie.immersiveenchanting.api.filter;

import me.alfie.alfinolib.util.ResourceId;
import org.jetbrains.annotations.Nullable;

/**
 * Current hold-to-filter selection on the enchanting table screen.
 *
 * <p>Global selections hide every enchantment claimed by a registered {@link IsolatedFilterBranch}. 
 * Isolated selections show only that branch's tag members.</p>
 */
public sealed interface EnchantmentFilterSelection
        permits EnchantmentFilterSelection.All,
                EnchantmentFilterSelection.Unlocked,
                EnchantmentFilterSelection.ModNamespace,
                EnchantmentFilterSelection.Isolated,
                EnchantmentFilterSelection.Search {

    /** Show every non-isolated enchantment. */
    record All() implements EnchantmentFilterSelection {}

    /** Show unlocked non-isolated enchantments only. */
    record Unlocked() implements EnchantmentFilterSelection {}

    /** Show non-isolated enchantments from one mod namespace. */
    record ModNamespace(String modid) implements EnchantmentFilterSelection {}

    /** Show only enchantments in the isolated branch identified by {@code branchId}. */
    record Isolated(ResourceId branchId) implements EnchantmentFilterSelection {}

    /** Universal search across all enchantments. Other filter modes are bypassed. */
    record Search() implements EnchantmentFilterSelection {}

    static EnchantmentFilterSelection all() {
        return new All();
    }

    static EnchantmentFilterSelection unlocked() {
        return new Unlocked();
    }

    static EnchantmentFilterSelection mod(@Nullable String modid) {
        if (modid == null) {
            return all();
        }
        return new ModNamespace(modid);
    }

    static EnchantmentFilterSelection isolated(ResourceId branchId) {
        return new Isolated(branchId);
    }

    static EnchantmentFilterSelection search() {
        return new Search();
    }
}
