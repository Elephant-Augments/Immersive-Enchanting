package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.gui.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;

public interface DescriptionLayoutExtension {
    void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip);
}
