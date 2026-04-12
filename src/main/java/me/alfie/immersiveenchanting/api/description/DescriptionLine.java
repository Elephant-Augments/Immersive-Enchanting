package me.alfie.immersiveenchanting.api.description;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface DescriptionLine {

    void render(GuiGraphicsExtractor graphics, int lineX, int lineY, double mouseX, double mouseY);

    /**
     * Return the text that is being drawn. This is used to expand the description box.
     * If this line doesn't draw text, you can return Component.empty().
     * Do not return null.
     * @return
     */
    @NotNull
    default Component getText() {
        return Component.empty();
    }
}
