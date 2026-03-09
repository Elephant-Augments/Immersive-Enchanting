package me.alfie.immersiveenchanting.api.internal.cost;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.tooltip.DescriptionLine;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public record LevelsDescriptionLine(NodeTooltip nodeTooltip) implements DescriptionLine {

    @Override
    public void draw(GuiGraphics graphics, int lineX, int lineY) {
        //Draw label
        graphics.drawString(Minecraft.getInstance().font,
                getText(),
                lineX,
                lineY + 4, //Offset to centre text with cost stack
                0xFFFFFF);

        Vector2i levelLabelPos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText()), lineY);

        //Draw XP sprite
        graphics.blit(
                Sprite.XP_LEVEL.get(),
                levelLabelPos.x,
                lineY,
                0, 0, 16, 16, 16, 16);

        //Draw XP level number
        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(String.valueOf(nodeTooltip.getCurrentRenderedCost().xpLevels())),
                levelLabelPos.x + 12,
                lineY + 4,
                0xC8FF8F
        );
    }

    @Override
    public @NotNull Component getText() {
        Component label = Component.translatable("gui.immersiveenchanting.cost.xp").withStyle(ChatFormatting.GRAY);
        return label;
    }
}
