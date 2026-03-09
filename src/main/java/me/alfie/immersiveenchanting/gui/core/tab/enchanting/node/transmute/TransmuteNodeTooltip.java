package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.transmute;

import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TransmuteNodeTooltip extends NodeTooltip {

    public TransmuteNodeTooltip(Node node, EnchantingTableScreen screen, List<CostEntry> validCosts) {
        super(node, screen, validCosts);

        Component text = Component.translatable("gui.immersiveenchanting.transmute_book").withStyle(ChatFormatting.WHITE);
        tooltipTitle.setTitleText(text);
    }
}
