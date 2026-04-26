package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.TooltipManager;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public record RemoveProgressLine(NodeTooltip tooltip) implements DescriptionLine {
    @Override
    public void render(GuiGraphics graphics, int lineX, int lineY, double mouseX, double mouseY) {
        final int width = Minecraft.getInstance().font.width(getText());
        final int height = Minecraft.getInstance().font.lineHeight;
        final int padding = 2;

        graphics.fill(lineX, lineY, lineX+width, lineY+height, Color.BLACK.getRGB());

        float progress = tooltip.screen()
                .tooltipManager()
                .getElapsedHeldTime() / (float) TooltipManager.HOLD_TRESHOLD_MILLIS;
        progress = Math.min(1f, Math.max(0f, progress));

        int innerX = lineX + padding;
        int innerY = lineY + padding;
        int innerWidth = width - (padding * 2);
        int innerHeight = height - (padding * 2);
        int filledWidth = (int) (innerWidth * progress);

        graphics.fill(innerX, innerY, innerX + filledWidth, innerY + innerHeight, 0xFFFF5555);

        FxHelper.playRemoveProgress(tooltip.screen().player(), progress);
    }

    @Override
    public @NotNull Component getText() {
        return Component.translatable("immersiveenchanting.tooltip.desc.equipped").withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
