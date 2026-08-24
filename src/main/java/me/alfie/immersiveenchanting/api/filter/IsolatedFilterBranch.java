package me.alfie.immersiveenchanting.api.filter;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.api.node.BuildBranchesEvent;
import me.alfie.immersiveenchanting.api.node.BranchBuilder;
import me.alfie.immersiveenchanting.api.node.ItemIcon;
import me.alfie.immersiveenchanting.api.node.NodeTemplate;
import me.alfie.immersiveenchanting.api.node.internal.FilterNodeData;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * An isolated filter branch for the hold-to-filter picker.
 *
 * <p>Isolated filters only show enchantments in their {@link #enchantmentTag()}, and those
 * enchantments are hidden from every {@link GlobalFilterBranch} view (including All).</p>
 *
 * <p>Register via {@link RegisterFilterBranchesEvent#registerIsolated(IsolatedFilterBranch)}.</p>
 */
public interface IsolatedFilterBranch {

    /**
     * Stable id for this branch.
     */
    ResourceId id();

    /**
     * Enchantment tag that defines this isolated pool.
     */
    TagKey<Enchantment> enchantmentTag();

    /**
     * Display title for the picker node.
     */
    Component title();

    /**
     * Icon shown on the picker node.
     */
    ItemStack createIconStack();

    /**
     * Visual tier for the picker node. Defaults to {@link NodeTier#ADVANCED}.
     */
    default NodeTier tier() {
        return NodeTier.ADVANCED;
    }

    /**
     * Picker branch id used in {@link BuildBranchesEvent}/angle layout.
     */
    default ResourceId pickerBranchId() {
        return new ResourceId(id().namespace(), "mod_filter/" + id().path());
    }

    default boolean contains(Holder<Enchantment> enchantment) {
        return enchantment.is(enchantmentTag());
    }

    default boolean shouldShow(Iterable<Holder<Enchantment>> applicableEnchantments) {
        for (Holder<Enchantment> enchantment : applicableEnchantments) {
            if (contains(enchantment)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Appends this isolated picker branch when {@link #shouldShow} is true.
     */
    default void addPickerBranch(BuildBranchesEvent event, boolean selected) {
        NodeState state = selected ? NodeState.OBTAINED : NodeState.UNOBTAINED;
        event.addBranch(BranchBuilder.of(event.getCanvas(), pickerBranchId())
                .node(new NodeTemplate(
                        title(),
                        0,
                        state,
                        tier(),
                        new ItemIcon(createIconStack()),
                        FilterNodeData.create(EnchantmentFilterSelection.isolated(id()))
                )).build());
    }
}
