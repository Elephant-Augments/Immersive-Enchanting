package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;

public interface DescriptionLayoutExtension {
    void extendLayout(DescriptionLayout description, NodeTooltip parentTooltip);
}
