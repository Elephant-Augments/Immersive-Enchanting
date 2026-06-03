package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.util.ResourceId;
import net.minecraft.client.renderer.RenderPipelines;

public class NineSliceBox {

    private ResourceId texture;
    private int x;
    private int y;
    private int width;
    private int height;

    public NineSliceBox(ResourceId texture) {
        this.texture = texture;

        //Fallback default dimensions
        this.width = 16;
        this.height = 16;
    }

    public void blitNineSliceSprite(GuiGraphicsX gx) {
        gx.graphics().blitSprite(
                RenderPipelines.GUI_TEXTURED,
                texture.mc(),
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
