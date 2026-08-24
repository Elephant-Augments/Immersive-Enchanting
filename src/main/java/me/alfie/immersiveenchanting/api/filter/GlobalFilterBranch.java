package me.alfie.immersiveenchanting.api.filter;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.api.node.BuildBranchesEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * A global filter interface for registering new filters into the hold-to-filter picker.
 *
 * <p>Global filters operate over all enchantments that are not claimed by any
 * {@link IsolatedFilterBranch}.</p>
 *
 * <p>Register via {@link RegisterFilterBranchesEvent#registerGlobal(GlobalFilterBranch)}.</p>
 */
public interface GlobalFilterBranch {

    /**
     * Stable id for this branch.
     */
    ResourceId id();

    /**
     * Appends a new global filter branch into the hold-to-filter picker.
     *
     * @param event                   branch build event
     * @param applicableEnchantments  enchantments applicable to the current item/context
     */
    void addPickerBranches(BuildBranchesEvent event, List<Holder<Enchantment>> applicableEnchantments);
}
