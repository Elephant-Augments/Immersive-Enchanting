package me.alfie.immersiveenchanting.api.enchanting_tab;

import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeType;

public record NodeTemplate(
        int level,
        NodeState state,
        NodeTier tier,
        NodeType type
) {}
