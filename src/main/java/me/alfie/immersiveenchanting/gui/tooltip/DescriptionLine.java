package me.alfie.immersiveenchanting.gui.tooltip;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface DescriptionLine {
    void draw(GuiGraphics graphics, int lineX, int lineY);
}
