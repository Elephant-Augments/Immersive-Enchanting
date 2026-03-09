package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.tooltip.DescriptionLayout;

public interface DescriptionLayoutExtension {
    void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip);
}
