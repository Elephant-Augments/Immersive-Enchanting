package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.network.chat.Component;

/**
 * Defines a template used to construct a node in the enchanting UI.
 * <p>
 * A {@code NodeTemplate} represents the static configuration of a node before
 * it is instantiated into an interactive {@link me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node}.
 * <p>
 * Templates define visual appearance, position, tier, state, and attached data/behavior.
 *
 * @param position index of the node within its branch
 * @param title    title text in the tooltip
 * @param icon     visual icon on the node
 * @param tier     progression tier or rarity classification of the node
 * @param state    state of the node (e.g. locked, available, obtained)
 * @param data     attached node data defining behavior and click handling
 */
public record NodeTemplate(
        int position, Component title,
        NodeIcon icon, NodeTier tier, NodeState state,
        NodeData<?> data
) {}
