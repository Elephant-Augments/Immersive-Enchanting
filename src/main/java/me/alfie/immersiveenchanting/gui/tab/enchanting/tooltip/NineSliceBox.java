package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class NineSliceBox {

    private ResourceLocation texture;
    private int x;
    private int y;
    private int width;
    private int height;

    public NineSliceBox(ResourceLocation texture) {
        this.texture = texture;

        //Fallback default dimensions
        this.width = 16;
        this.height = 16;
    }

    public void blitNineSliceSprite(GuiGraphics graphics) {
        graphics.blitSprite(
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
