package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Populates the tooltip description for the Transmute node based on its current state:
 * <ul>
 *   <li>UNOBTAINED – shows the transmute description text followed by the cost lines</li>
 *   <li>LOCKED – shows a hint explaining why transmute is unavailable</li>
 *   <li>ALERT – indicates the held item cannot be transmuted</li>
 * </ul>
 * No-ops for all other nodes.
 */
public class TransmuteLayoutExtension implements DescriptionLayoutExtension {
    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().branchId().equals(CostRegistry.TRANSMUTE)) return;

        description.widthPadding = 16;

        int linesCreated = 0;
        if(tooltip.node().isState(NodeState.UNOBTAINED)) {
            linesCreated = DescriptionHelper.lineWrapComponent(
                    Component.translatable("immersiveenchanting.tooltip.desc.transmute")
                            .withStyle(ChatFormatting.GRAY),
                    DescriptionHelper.DEFAULT_LINE_WIDTH, description, 0);
        } else if (tooltip.node().isState(NodeState.LOCKED)) {
            linesCreated = DescriptionHelper.lineWrapComponent(
                    Component.translatable("immersiveenchanting.tooltip.desc.transmute_hint")
                            .withStyle(ChatFormatting.GRAY),
                    DescriptionHelper.DEFAULT_LINE_WIDTH, description, 0);
        } else if (tooltip.node().isState(NodeState.ALERT)) {
            linesCreated = DescriptionHelper.lineWrapComponent(
                    Component.translatable("immersiveenchanting.tooltip.desc.not_transmutable")
                            .withStyle(ChatFormatting.GRAY),
                    DescriptionHelper.DEFAULT_LINE_WIDTH, description, 0);
        }

        if(tooltip.node().isState(NodeState.UNOBTAINED)) DescriptionHelper.insertCostLines(tooltip, description, linesCreated+1);

    }
}
