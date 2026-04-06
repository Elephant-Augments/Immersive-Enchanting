package me.alfie.immersiveenchanting.gui.canvas;

import me.alfie.immersiveenchanting.gui.Sprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Base class for objects that can be rendered on a {@link Canvas} without
 * manually applying scroll offsets. Provides methods for position, scaling,
 * and drawing textures or sprites.
 */
public abstract class CanvasRenderable {

    private float canvasX;
    private float canvasY;
    private float scale = 1f;
    private final Canvas canvas;

    //Used when applying offsets i.e setScaleKeepPos()
    private float baseCanvasX;
    private float baseCanvasY;

    /**
     * Creates a new CanvasRenderable bound to the specified canvas.
     *
     * @param canvas The canvas this object will render on
     */
    public CanvasRenderable(Canvas canvas) {
        this.canvas = canvas;
    }

    /**
     * Render this object on the canvas. Called every frame.
     *
     * @param graphics The graphics context
     * @param mouseX Current mouse X position relative to GUI
     * @param mouseY Current mouse Y position relative to GUI
     */
    public abstract void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY);

    /**
     * Gets the canvas this object is attached to.
     *
     * @return The canvas
     */
    public Canvas canvas() {
        return canvas;
    }

    /**
     * Sets the canvas position of this object.
     *
     * @param x X coordinate on the canvas
     * @param y Y coordinate on the canvas
     */
    public void setCanvasPos(float x, float y) {
        this.canvasX = x;
        this.canvasY = y;
        this.baseCanvasX = x;
        this.baseCanvasY = y;
    }

    /**
     * Sets the object’s position so that it is centered on the canvas,
     * based on the texture’s width and height.
     *
     * @param textureWidth Width of the object
     * @param textureHeight Height of the object
     */
    public void setCenterPos(int textureWidth, int textureHeight) {
        setCanvasPos(canvas().getCenter().x() - (float) textureWidth / 2,
                canvas().getCenter().y() - (float) textureHeight / 2);
    }

    /**
     * Sets the object’s position so that it is centered on the canvas
     * based on a {@link Sprite}.
     *
     * @param sprite The sprite to center
     */
    public void setCenterPos(Sprite sprite) {
        setCenterPos(sprite.width(), sprite.height());
    }

    /**
     * Gets the current X coordinate on the canvas.
     *
     * @return Canvas X position
     */
    public float canvasX() {
        return canvasX;
    }

    /**
     * Gets the current Y coordinate on the canvas.
     *
     * @return Canvas Y position
     */
    public float canvasY() {
        return canvasY;
    }

    /**
     * Gets the current scale of the object.
     *
     * @return Scale factor
     */
    public float scale() {
        return scale;
    }

    /**
     * Sets the scale of this object.
     *
     * @param scale Scale factor
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Returns the length (i.e width/height) scaled by the current scale factor.
     *
     * @param length Original length (i.e width/height)
     * @return Scaled length
     */
    public float getScaledLength(int length) {
        return length * scale();
    }

    /**
     * Sets the scale while keeping the object centered in its original position.
     * Adjusts canvas coordinates accordingly.
     *
     * @param scale The new scale factor
     * @param sprite The sprite to scale
     */
    public void setScaleKeepPos(float scale, Sprite sprite) {
        this.scale = scale;
        float offsetX = (sprite.width() - sprite.width() * scale) / 2f;
        float offsetY = (sprite.height() - sprite.height() * scale) / 2f;

        canvasX = baseCanvasX + offsetX;
        canvasY = baseCanvasY + offsetY;
    }

    /**
     * Draws a {@link Sprite} at the object’s canvas position.
     *
     * @param graphics The graphics context
     * @param sprite The sprite to draw
     */
    public void blit(GuiGraphicsExtractor graphics, Sprite sprite) {
        blit(graphics, sprite.id(), sprite.width(), sprite.height());
    }

    /**
     * Draws a texture at the object’s canvas position.
     *
     * @param graphics The graphics context
     * @param id The texture identifier
     * @param width Width of the texture
     * @param height Height of the texture
     */
    public void blit(GuiGraphicsExtractor graphics, Identifier id,
                     int width, int height) {
        blit(graphics, id, width, height, 0, 0);
    }

    /**
     * Draws a texture at the canvas position, optionally offset by pixels.
     *
     * WARNING: Offsets modify the transform matrix directly. Do NOT use this for
     * objects that rely on logical canvasX/Y (like mouse hover checks).
     * Use the other blit() overloads for those.
     *
     * @param graphics The graphics context
     * @param id The texture identifier
     * @param width Width of the texture
     * @param height Height of the texture
     * @param offsetX Pixel offset on the X axis
     * @param offsetY Pixel offset on the Y axis
     */
    public void blit(GuiGraphicsExtractor graphics, Identifier id,
                           int width, int height,
                           int offsetX, int offsetY) {
        graphics.pose().pushMatrix();

        graphics.pose().translate(canvasX(), canvasY());
        graphics.pose().scale(scale());
        graphics.pose().translate(-canvasX(), -canvasY());

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                id,
                (int) canvasX() + offsetX, (int) canvasY() + offsetY,
                0, 0,
                width, height,
                width, height
        );

        graphics.pose().popMatrix();
    }
}
