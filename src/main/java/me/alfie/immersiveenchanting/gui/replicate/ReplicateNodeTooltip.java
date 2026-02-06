package me.alfie.immersiveenchanting.gui.replicate;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Node;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ReplicateNodeTooltip extends NodeTooltip {

    public ReplicateNodeTooltip(Node node, EnchantingTableScreen screen) {
        super(node, screen);

        String text = Component.translatable("gui.immersiveenchanting.replicate_book").withStyle(ChatFormatting.GOLD).getString();
        tooltipTitle.setTitleText(text);
    }
}
