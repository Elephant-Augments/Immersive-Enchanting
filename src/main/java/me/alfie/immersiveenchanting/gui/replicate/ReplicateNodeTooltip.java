package me.alfie.immersiveenchanting.gui.replicate;

import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Node;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ReplicateNodeTooltip extends NodeTooltip {

    public ReplicateNodeTooltip(Node node, EnchantingTableScreen screen, List<CostEntry> validCosts) {
        super(node, screen, validCosts);

        Component text = Component.translatable("gui.immersiveenchanting.replicate_book").withStyle(ChatFormatting.WHITE);
        tooltipTitle.setTitleText(text);
    }
}
