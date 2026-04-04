package me.alfie.immersiveenchanting.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Vector2i;

public class ScrollableCanvas implements ScreenEventListener {

    private final EnchantingTableScreen screen;

    private int width;
    private int height;
    private int tiles;
    private double x;
    private double y;
    private float scale;
    private boolean dragging;
    private boolean dragLocked;

    private static final int VIEWPORT_WIDTH = 247;
    private static final int VIEWPORT_HEIGHT = 117;

    private final TiledBackground background;

    public ScrollableCanvas(EnchantingTableScreen screen) {
        this.background = new TiledBackground(this);
        this.screen = screen;

        resizeAndCenter(16);
    }

    public EnchantingTableScreen screen() {
        return screen;
    }

    public int tiles() {
        return tiles;
    }

    /**
     * Resize the canvas and center the canvas in the viewport.
     * @param tileCount - Number of tiles in the x/y-axis.
     */
    public void resizeAndCenter(int tileCount) {
        tiles = tileCount;
        width = tiles * TiledBackground.TILE_SIZE;
        height = tiles * TiledBackground.TILE_SIZE;

        x = (VIEWPORT_WIDTH - width) / 2.0 + 5;
        y = (VIEWPORT_HEIGHT - height) / 2.0 + 5;
    }

    public Vector2i getLocalCenterPos(int textureWidth, int textureHeight) {
        int x = width / 2 - textureWidth / 2;
        int y = height / 2 - textureHeight / 2;
        return new Vector2i(x, y);
    }

    public void render(GuiGraphicsExtractor graphics) {
        background.render(graphics, 0, 0);
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent mouse) {
        final int viewportLeft = screen.getGuiLeft() + 5;
        final int viewportTop = screen.getGuiTop() + 5;
        final int viewportRight = viewportLeft + VIEWPORT_WIDTH;
        final int viewportBottom = viewportTop + VIEWPORT_HEIGHT;

        if (mouse.x() >= viewportLeft && mouse.y() < viewportRight && mouse.y() >= viewportTop && mouse.y() < viewportBottom
                && !dragLocked) {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent mouse, double dx, double dy) {
        if (!dragging) return false;

        final int viewportLeft = screen.getGuiLeft() + 5;
        final int viewportTop = screen.getGuiTop() + 5;
        final int viewportRight = viewportLeft + VIEWPORT_WIDTH;
        final int viewportBottom = viewportTop + VIEWPORT_HEIGHT;

        //Apply drag delta
        this.x += dx;
        this.y += dy;

        //Convert bounds into local space
        double minX = viewportLeft - screen.getGuiLeft();
        double minY = viewportTop - screen.getGuiTop();
        double maxX = viewportRight - screen.getGuiLeft() - width;
        double maxY = viewportBottom - screen.getGuiTop() - height;

        //Clamp to bounds
        this.x = Math.max(maxX, Math.min(this.x, minX));
        this.y = Math.max(maxY, Math.min(this.y, minY));

        return true;
    }

    @Override
    public boolean onMouseRelease(MouseButtonEvent mouse) {
        if(!dragging) return false;

        dragging = false;
        return true;
    }

    public int getLocalX() {
        return (int) Math.round(x) + screen.getGuiLeft();
    }

    public int getLocalY() {
        return  (int) Math.round(y) + screen.getGuiTop();
    }

    /**
     * Check if mouse is over a bounding box. Automatically clips the bounding box if out of viewport bounds.
     * <br>Must pass local canvas coordinates!
     * @param x Top Left position of the bounding box to detect
     * @param y Top Left position of the bounding box to detect
     * @param width      Width of the bounding box to detect
     * @param height     Height of the bounding box to detect
     * @param mouseX
     * @param mouseY
     * @return
     */
    public boolean isMouseOver(
            int x, int y,
            int width, int height,
            double mouseX, double mouseY) {
        int drawX = x;
        int drawY = y;

        int boxLeft = drawX;
        int boxTop = drawY;
        int boxRight = drawX + width;
        int boxBottom = drawY + height;

        // Viewport bounds
        int viewportLeft = screen.getGuiLeft() + 5;
        int viewportTop = screen.getGuiTop() + 5;
        int viewportRight = viewportLeft + VIEWPORT_WIDTH;
        int viewportBottom = viewportTop + VIEWPORT_HEIGHT;

        // Clip node bounds to viewport
        int visibleLeft = Math.max(boxLeft, viewportLeft);
        int visibleTop = Math.max(boxTop, viewportTop);
        int visibleRight = Math.min(boxRight, viewportRight);
        int visibleBottom = Math.min(boxBottom, viewportBottom);

        // If the node is fully outside the viewport, return false
        if (visibleLeft >= visibleRight || visibleTop >= visibleBottom) return false;

        // Check if mouse is over the visible part
        return mouseX >= visibleLeft && mouseX < visibleRight
                && mouseY >= visibleTop && mouseY < visibleBottom;
    }

    public void setDragLocked(boolean dragLocked) {
        this.dragLocked = dragLocked;
    }

    public boolean isDragLocked() {
        return dragLocked;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float scale() {
        return scale;
    }
}
