package me.alfie.immersiveenchanting.gui.tooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface DescriptionLine {
    void draw(GuiGraphics graphics, int lineX, int lineY);

    /**
     * Return the text that is being drawn. This is used to expand the description box.
     * If this line doesn't draw text, you can return Component.empty().
     * Do not return null.
     * @return
     */
    @NotNull
    Component getText();
}
