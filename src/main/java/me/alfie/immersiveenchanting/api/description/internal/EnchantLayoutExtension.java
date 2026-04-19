package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.*;
import me.alfie.immersiveenchanting.api.description.internal.lines.*;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.world.item.Items;

public class EnchantLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().isEnchantment()) return;

        description.widthPadding = 16;
        tooltip.screen().enchantmentCostRenderer().setCostToRender(tooltip.node().id(), tooltip.node().getEnchantmentLevel());

        if(tooltip.node().isState(NodeState.UNOBTAINED)) {
            DescriptionHelper.insertCostLines(tooltip, description, 0);
        } else if(tooltip.node().isState(NodeState.OBTAINED)) {
            description.insertLine(0, new EquippedLine(tooltip));
        } else if(tooltip.node().isState(NodeState.LOCKED)) {
            description.insertLine(0, new UnavailableEnchantmentLine(tooltip));
        }
    }
}
