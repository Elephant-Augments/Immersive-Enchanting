package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.network.chat.Component;

public record NodeTemplate(
        Component title,
        int level,
        NodeState state,
        NodeTier tier,
        NodeIcon icon,
        NodeData<?> data
) {}
