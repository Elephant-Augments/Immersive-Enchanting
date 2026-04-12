package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.*;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;

public class EnchantingLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().isEnchantment()) return;

        description.widthPadding = 16;
        tooltip.screen().enchantmentCostRenderer().setCostToRender(tooltip.node().id(), tooltip.node().getEnchantmentLevel());

        description.insertLine(0, new MaterialsLine(tooltip));
        description.insertLine(2, new FuelsLine(tooltip));

        if(tooltip.screen().enchantmentCostRenderer().getCurrentRenderedCost().xpLevels() > 0 ) description.insertLine(4, new LevelsLine(tooltip));
    }
}
