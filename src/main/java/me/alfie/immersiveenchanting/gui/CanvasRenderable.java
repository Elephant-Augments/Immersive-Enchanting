package me.alfie.immersiveenchanting.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Provides methods to render an object on the canvas without calculating scroll offsets.
 */
public abstract class CanvasRenderable {

    private double x;
    private double y;
    private final ScrollableCanvas canvas;
    private float scale = 1f;

    public CanvasRenderable(ScrollableCanvas canvas) {
        this.canvas = canvas;
    }

    public abstract void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY);

    public ScrollableCanvas canvas() {
        return canvas;
    }

    public int getScreenX() {
        return (int) Math.round(x + canvas().getLocalX());
    }

    /**
     * Set the x position to render at.
     * @param x The x pos in local coordinates (0, 0) is the top-left of the canvas.
     */
    public void setX(double x) {
        this.x = x;
    }

    public int getScreenY() {
        return (int) Math.round(y + canvas().getLocalY());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /**
     * Set the y position to render at.
     * @param y The y pos in local coordinates (0, 0) is the top-left of the canvas.
     */
    public void setY(double y) {
        this.y = y;
    }

    public void setPos(double x, double y) {
        setX(x);
        setY(y);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }

    /**
     * Helper to quickly draw a sprite. Places at getScreenX()/getScreenY().
     * @param sprite
     * @param graphics
     */
    public void blit(Sprite sprite, GuiGraphicsExtractor graphics) {
        sprite.draw(graphics, getScreenX(), getScreenY());
    }

    public void blit(Identifier id, int texWidth, int texHeight, GuiGraphicsExtractor graphics) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                id,
                getScreenX(), getScreenY(),
                0, 0,
                texWidth, texHeight,
                texWidth, texHeight
        );
    }
}
