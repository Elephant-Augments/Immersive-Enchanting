package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.HoverableItemStack;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.RenderedCost;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record MaterialsLine(NodeTooltip tooltip) implements DescriptionLine {

    /**
     * Renders the "Materials:" label and then draws the required material item stack
     * immediately to the right of the label text.
     */
    @Override
    public void render(GuiGraphics graphics, int lineX, int lineY, double mouseX, double mouseY) {
        DescriptionHelper.text(graphics, getText(), lineX, lineY);

        EnchantingTableScreen screen = tooltip.screen();
        RenderedCost renderedCost = screen.enchantmentCostRenderer().getCurrentRenderedCost();

        HoverableItemStack stack = new HoverableItemStack(screen, renderedCost.stack());
        stack.setPos(lineX + Minecraft.getInstance().font.width(getText().getString()),
                lineY - 4);
        stack.render(graphics, mouseX, mouseY);
    }

    @Override
    public @NotNull Component getText() {
        return Component.translatable("immersiveenchanting.tooltip.desc.materials").withStyle(ChatFormatting.GRAY);
    }
}
