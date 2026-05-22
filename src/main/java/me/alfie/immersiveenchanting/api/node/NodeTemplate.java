package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.network.chat.Component;

/**
 * Immutable descriptor used by {@link BranchBuilder} to define a node before it is
 * materialised into a live {@link me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node}.
 *
 * <p>Carries everything needed to construct the node: display title, enchantment level,
 * visual state, tier, icon, and the typed {@link NodeData} that drives click behaviour.
 */
public record NodeTemplate(
        Component title,
        int level,
        NodeState state,
        NodeTier tier,
        NodeIcon icon,
        NodeData<?> data
) {
}
