package me.alfie.immersiveenchanting.gui.canvas;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.ScreenEventListener;
import net.minecraft.client.input.MouseButtonEvent;

public class CanvasCamera implements ScreenEventListener {
    private float x;
    private float y;
    private float zoom = 1f;

    private boolean dragging;
    private boolean draggingEnabled;
    private final float ZOOM_STEP = 0.1f;
    private final float MIN_ZOOM = 0.8f;
    private final float MAX_ZOOM = 1.5f;

    private final EnchantingTableScreen screen;

    public final int VIEWPORT_X;
    public final int VIEWPORT_Y;
    public final int VIEWPORT_WIDTH = 248;
    public final int VIEWPORT_HEIGHT = 118;

    public CanvasCamera(EnchantingTableScreen screen, int viewportX, int viewportY) {
        this.VIEWPORT_X = viewportX;
        this.VIEWPORT_Y = viewportY;
        this.screen = screen;
    }

    public void setDraggingEnabled(boolean enabled) {
        this.draggingEnabled = enabled;
    }

    public boolean isDraggingEnabled() {
        return draggingEnabled;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float zoom() {
        return zoom;
    }

    public void setPos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void centerCameraOnCanvas() {
        setPos(
                screen.canvas().getCenter().x - (float) VIEWPORT_WIDTH / (2 * zoom),
                screen.canvas().getCenter().y - (float) VIEWPORT_HEIGHT / (2 * zoom)
        );
        clampPosition();
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        clampPosition();
    }

    public void move(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent mouse) {
        if(isMouseOverViewport(mouse.x(), mouse.y()) && isDraggingEnabled()) {
            dragging = true;
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouse);
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent event, double dx, double dy) {
        if(dragging) {
            move((float) -dx, (float) -dy);
            clampPosition();

            return true;
        }

        return ScreenEventListener.super.onMouseDrag(event, dx, dy);
    }

    public void clampPosition() {
        float maxX = screen.canvas().getSize() - VIEWPORT_WIDTH / zoom;
        float maxY = screen.canvas().getSize() - VIEWPORT_HEIGHT / zoom;

        setPos(Math.max(0, Math.min(x, maxX)), Math.max(0, Math.min(y, maxY)));
    }

    @Override
    public boolean onMouseRelease(MouseButtonEvent mouse) {
        dragging = false;
        return false;
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY) {
        if(isMouseOverViewport(mouseX, mouseY) && isDraggingEnabled()) {
            float newZoom = zoom() + (float)(ZOOM_STEP * scrollY);
            newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
            setZoom(newZoom);
        }

        return ScreenEventListener.super.onMouseScrolled(mouseX, mouseY, scrollY);
    }

    public boolean isMouseOverViewport(double mouseX, double mouseY) {
        return mouseX >= VIEWPORT_X &&
                mouseX <= VIEWPORT_X + VIEWPORT_WIDTH &&
                mouseY >= VIEWPORT_Y &&
                mouseY <= VIEWPORT_Y + VIEWPORT_HEIGHT;
    }
}
