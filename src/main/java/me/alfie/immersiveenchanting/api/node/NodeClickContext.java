package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;

public record NodeClickContext(
        Node node,
        EnchantingTableScreen screen) {
}
