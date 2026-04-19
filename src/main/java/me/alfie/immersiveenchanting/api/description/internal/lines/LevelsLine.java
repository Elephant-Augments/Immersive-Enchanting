package me.alfie.immersiveenchanting.api.description.internal.lines;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLine;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.RenderedCost;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record LevelsLine(NodeTooltip tooltip) implements DescriptionLine {

    @Override
    public void render(GuiGraphicsExtractor graphics, int lineX, int lineY, double mouseX, double mouseY) {
        DescriptionHelper.text(graphics, getText(), lineX, lineY);

        EnchantingTableScreen screen = tooltip.screen();
        RenderedCost renderedCost = screen.enchantmentCostRenderer().getCurrentRenderedCost();

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Sprite.XP_LEVEL.id(),
                lineX + Minecraft.getInstance().font.width(getText().getString()),
                lineY - 4,
                0, 0,
                Sprite.XP_LEVEL.width(), Sprite.XP_LEVEL.height(),
                Sprite.XP_LEVEL.width(), Sprite.XP_LEVEL.height());

        DescriptionHelper.text(graphics,
                Component.literal(String.valueOf(renderedCost.xpLevels())).withColor(0xC8FF8F),
                lineX + Minecraft.getInstance().font.width(getText().getString()) + 10,
                lineY + 4);

    }

    @Override
    public @NotNull Component getText() {
        return Component.translatable("immersiveenchanting.tooltip.desc.xp_levels").withStyle(ChatFormatting.GRAY);
    }
}
