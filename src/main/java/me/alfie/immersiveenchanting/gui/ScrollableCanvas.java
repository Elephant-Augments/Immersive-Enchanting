package me.alfie.immersiveenchanting.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
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

    private static final int MIN_WIDTH = 256;
    private static final float MAX_SCALE = 2f;
    private static final int VIEWPORT_LEFT = 4;
    private static final int VIEWPORT_TOP = 4;
    private static final int VIEWPORT_WIDTH = 247;
    private static final int VIEWPORT_HEIGHT = 117;

    private final TiledBackground background;

    public ScrollableCanvas(EnchantingTableScreen screen) {
        this.background = new TiledBackground(this);
        this.screen = screen;

        setScale(1f);
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

        x = (VIEWPORT_WIDTH / scale - width) / 2.0 + VIEWPORT_LEFT / scale;
        y = (VIEWPORT_HEIGHT / scale - height) / 2.0 + VIEWPORT_TOP / scale;
    }

    private void clampInsideViewport() {
        //Convert bounds into local space
        double minX = VIEWPORT_WIDTH + VIEWPORT_LEFT - (width * scale);
        double maxX = VIEWPORT_LEFT;
        double minY = VIEWPORT_HEIGHT + VIEWPORT_TOP - (height * scale);
        double maxY = VIEWPORT_TOP;

        //Clamp to bounds
        this.x = Math.max(minX / scale, Math.min(this.x, maxX / scale));
        this.y = Math.max(minY / scale, Math.min(this.y, maxY / scale));
    }

    public Vector2i getLocalCenterPos(int textureWidth, int textureHeight) {
        int x = width / 2 - textureWidth / 2;
        int y = height / 2 - textureHeight / 2;
        return new Vector2i(x, y);
    }

    public Rect getViewportRect() {
        final int viewportLeft = screen.getGuiLeft() + VIEWPORT_LEFT;
        final int viewportTop = screen.getGuiTop() + VIEWPORT_TOP;

        return new Rect(viewportLeft, viewportTop, viewportLeft+VIEWPORT_WIDTH, viewportTop+VIEWPORT_HEIGHT);
    }

    public void render(GuiGraphicsExtractor graphics) {
        background.render(graphics, 0, 0);
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent mouse) {
        if(isMouseOverViewport(mouse.x(), mouse.y()) && !dragLocked) {
            dragging = true;
            return true;
        }
        return ScreenEventListener.super.onMouseClick(mouse);
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent mouse, double dx, double dy) {
        if (!dragging) return ScreenEventListener.super.onMouseDrag(mouse, dx, dy);

        this.x += dx / scale;
        this.y += dy / scale;
        clampInsideViewport();

        return true;
    }

    @Override
    public boolean onMouseRelease(MouseButtonEvent mouse) {
        if(!dragging) return ScreenEventListener.super.onMouseRelease(mouse);

        dragging = false;
        return true;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY) {
        if(isMouseOverViewport(mouseX, mouseY) && !dragLocked) {
            final float zoomStep = 0.1f;

            float newScale = (float) (scale() + zoomStep*scrollY);
            if(newScale * width < MIN_WIDTH || newScale > MAX_SCALE) return false;


            double centerX = (VIEWPORT_WIDTH / 2.0) / scale - x;
            double centerY = (VIEWPORT_HEIGHT / 2.0) / scale - y;
            x = (VIEWPORT_WIDTH / 2.0) / newScale - centerX;
            y = (VIEWPORT_HEIGHT / 2.0) / newScale - centerY;

            setScale(newScale);
            clampInsideViewport();

            return true;
        }
        return ScreenEventListener.super.onMouseScrolled(mouseX, mouseY, scrollY);
    }

    public int getLocalX() {
        return (int) (x + screen.getGuiLeft() / scale);
    }

    public int getLocalY() {
        return (int) (y + screen.getGuiTop() / scale);
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
        int boxLeft = (int) (x*scale);
        int boxTop = (int) (y*scale);
        int boxRight = (int) (boxLeft + (width * scale));
        int boxBottom = (int) (boxTop + (height * scale));

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

    public boolean isMouseOverViewport(double mouseX, double mouseY) {
        Rect rect = getViewportRect();
        return mouseX >= rect.x1() && mouseX < rect.x2()
                && mouseY >= rect.y1() && mouseY < rect.y2();
    }

    public void setDragLocked(boolean dragLocked) {
        this.dragLocked = dragLocked;
        setScale(1f);
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
