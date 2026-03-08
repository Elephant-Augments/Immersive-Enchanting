package me.alfie.immersiveenchanting.gui.transmute;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Node;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class TransmuteNodeTooltip extends NodeTooltip {

    public TransmuteNodeTooltip(Node node, EnchantingTableScreen screen) {
        super(node, screen);

        Component text = Component.translatable("gui.immersiveenchanting.transmute_book").withStyle(ChatFormatting.GOLD);
        tooltipTitle.setTitleText(text);
    }
}
