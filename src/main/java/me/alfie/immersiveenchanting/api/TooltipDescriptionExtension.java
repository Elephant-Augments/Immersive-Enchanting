package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.gui.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.tooltip.DescriptionLayout;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipDescription;

public interface TooltipDescriptionExtension {
    void extendLayout(DescriptionLayout description, EnchantingNodeTooltip parentTooltip);
}
