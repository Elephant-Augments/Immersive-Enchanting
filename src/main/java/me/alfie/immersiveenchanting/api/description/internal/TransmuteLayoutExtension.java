package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TransmuteLayoutExtension implements DescriptionLayoutExtension {
    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().id().equals(CostRegistry.TRANSMUTE)) return;

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
