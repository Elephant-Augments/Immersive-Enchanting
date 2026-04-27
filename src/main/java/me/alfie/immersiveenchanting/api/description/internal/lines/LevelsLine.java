package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.RenderedCost;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record LevelsLine(NodeTooltip tooltip) implements DescriptionLine {

    @Override
    public void render(GuiGraphics graphics, int lineX, int lineY, double mouseX, double mouseY) {
        DescriptionHelper.text(graphics, getText(), lineX, lineY);

        EnchantingTableScreen screen = tooltip.screen();
        RenderedCost renderedCost = screen.enchantmentCostRenderer().getCurrentRenderedCost();

        graphics.blit(
                Sprite.XP_LEVEL.id(),
                lineX + Minecraft.getInstance().font.width(getText().getString()),
                lineY - 4,
                0, 0,
                Sprite.XP_LEVEL.width(), Sprite.XP_LEVEL.height(),
                Sprite.XP_LEVEL.width(), Sprite.XP_LEVEL.height());

        graphics.drawString(
                Minecraft.getInstance().font,
                Component.literal(String.valueOf(renderedCost.xpLevels())),
                lineX + Minecraft.getInstance().font.width(getText().getString()) + 10,
                lineY + 4,
                0xC8FF8F
        );
    }

    @Override
    public @NotNull Component getText() {
        return Component.translatable("immersiveenchanting.tooltip.desc.xp_levels").withStyle(ChatFormatting.GRAY);
    }
}
