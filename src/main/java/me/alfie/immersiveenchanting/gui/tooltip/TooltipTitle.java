package me.alfie.immersiveenchanting.gui.tooltip;

import me.alfie.immersiveenchanting.gui.EnchantingNodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class TooltipTitle extends NineSliceBox {

    private String titleText;

    public TooltipTitle(EnchantingNodeTooltip parentTooltip, ResourceLocation spriteTexture) {
        super(parentTooltip, spriteTexture);
    }

    public final void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    @Override
    public void draw(GuiGraphics graphics) {
        super.draw(graphics);

        //Draw title text
        int padding = 8;
        int iconSize = 26;
        //Add offset if renderDirection is left to right (makes room for the node)
        final int xFlipOffset = this.parentTooltip.getRenderDirection().isFlippedX() ? 0 : iconSize - padding / 2;
        final int titleTextX = this.getX() + padding / 2 + xFlipOffset;
        final int titleTextY = this.getY() + padding - 1;

        //Draw contents
        graphics.drawString(this.parentTooltip.getFont(),
                titleText,
                titleTextX,
                titleTextY,
                ChatFormatting.WHITE.getColor());
    }
}
