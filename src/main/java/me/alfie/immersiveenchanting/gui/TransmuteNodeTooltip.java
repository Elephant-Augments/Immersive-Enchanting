package me.alfie.immersiveenchanting.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TransmuteNodeTooltip extends NodeTooltip {

    public TransmuteNodeTooltip(Node node, EnchantingTableScreen screen) {
        super(node, screen);

        String text = Component.translatable("gui.immersiveenchanting.transmute_book").withStyle(ChatFormatting.GOLD).getString();
        tooltipTitle.setTitleText(text);
    }
}
