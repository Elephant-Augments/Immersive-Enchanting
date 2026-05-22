package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Populates the tooltip description for the Replicate node.
 * Always shows the replicate description text followed by the cost lines,
 * regardless of node state. No-ops for all other nodes.
 */
public class ReplicateLayoutExtension implements DescriptionLayoutExtension {
    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if (!tooltip.node().branchId().equals(CostRegistry.REPLICATE)) return;

        description.widthPadding = 16;

        int linesCreated = DescriptionHelper.lineWrapComponent(
                Component.translatable("immersiveenchanting.tooltip.desc.replicate")
                        .withStyle(ChatFormatting.GRAY),
                DescriptionHelper.DEFAULT_LINE_WIDTH, description, 0);

        DescriptionHelper.insertCostLines(tooltip, description, linesCreated + 1);

    }
}
