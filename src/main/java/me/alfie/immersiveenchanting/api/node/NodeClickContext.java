package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;

/**
 * Context provided when a node is clicked in the enchanting UI.
 * <p>
 * Contains references to the current screen and the node that was interacted with.
 * This is passed to {@code NodeData} click handlers.
 *
 * @param screen the active {@link EnchantingTableScreen} instance
 * @param node   the {@link Node} that was clicked
 */
public record NodeClickContext(EnchantingTableScreen screen, Node node) {
}
