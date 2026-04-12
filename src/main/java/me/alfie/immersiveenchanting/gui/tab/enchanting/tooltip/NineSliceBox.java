package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class NineSliceBox {

    private Identifier texture;
    private int x;
    private int y;
    private int width;
    private int height;

    public NineSliceBox(Identifier texture) {
        this.texture = texture;

        //Fallback default dimensions
        this.width = 16;
        this.height = 16;
    }

    public void blitNineSliceSprite(GuiGraphicsExtractor graphics) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                texture,
                x, y, width, height
        );
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

}
