package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.description.internal.lines.*;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;

public class EnchantLayoutExtension implements DescriptionLayoutExtension {

    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().isEnchantment()) return;

        description.widthPadding = 16;

        Node node = tooltip.node();
        EnchantingTableScreen screen = tooltip.screen();

        if(node.isState(NodeState.UNOBTAINED)) {
            DescriptionHelper.insertCostLines(tooltip, description, 0);
        } else if(node.isState(NodeState.OBTAINED)) {
            if(screen.tooltipManager().isHoldingTooltip()) {
                description.insertLine(0, new RemovingLine(tooltip));
                description.insertLine(1, new RemoveProgressLine(tooltip));
            } else {
                description.insertLine(0, new EquippedLine(tooltip));

                if(tooltip.canRemove()) {
                    description.insertLine(1, new RemoveHintLine(tooltip));
                }

            }
        } else if(node.isState(NodeState.LOCKED)) {
            description.insertLine(0, new UnavailableEnchantmentLine(tooltip));
        }
    }
}
