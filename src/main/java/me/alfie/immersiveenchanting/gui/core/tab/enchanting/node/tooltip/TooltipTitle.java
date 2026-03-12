package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.tooltip;

import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TooltipTitle extends NineSliceBox {

    private Component titleText;

    public TooltipTitle(NodeTooltip parentTooltip, ResourceLocation spriteTexture) {
        super(parentTooltip, spriteTexture);
    }

    public final void setTitleText(Component titleText) {
        this.titleText = titleText;
    }

    public Component getTitleText() {
        return titleText;
    }

    @Override
    public void draw(GuiGraphics graphics, int uvOffset) {
        super.draw(graphics, uvOffset);

        //Draw title text
        int padding = 8;
        int iconSize = 26;
        //Add offset if renderDirection is left to right (makes room for the node)
        final int xFlipOffset = this.parentTooltip.getRenderDirection().isFlippedX() ? 0 : iconSize - padding / 2;
        final int titleTextX = this.getX() + padding / 2 + xFlipOffset;
        final int titleTextY = this.getY() + padding - 1;

        //Draw contents
        graphics.drawString(Minecraft.getInstance().font,
                titleText,
                titleTextX,
                titleTextY,
                0xFFFFFF);
    }
}
