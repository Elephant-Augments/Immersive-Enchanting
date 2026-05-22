package me.alfie.immersiveenchanting.api.description;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface DescriptionLine {

    /**
     * Render this line at the given position.
     *
     * @param lineX  The left edge of the description box content area
     * @param lineY  The top of this line, already offset for its position in the layout
     * @param mouseX Current mouse X, forwarded for hover effects (e.g. item tooltips)
     * @param mouseY Current mouse Y, forwarded for hover effects (e.g. item tooltips)
     */
    void render(GuiGraphics graphics, int lineX, int lineY, double mouseX, double mouseY);

    /**
     * Return the text that is being drawn. This is used to expand the description box.
     * If this line doesn't draw text, you can return Component.empty().
     * Do not return null.
     *
     * @return
     */
    @NotNull
    default Component getText() {
        return Component.empty();
    }
}
