package me.alfie.immersiveenchanting.gui.canvas;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.BranchManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class Canvas implements ScreenEventListener {

    private final EnchantingTableScreen screen;
    public boolean DEBUG_DISABLE_CULLING = false;

    private int backgroundTileCount;
    private int width;
    private int height;

    private static final int TILE_SIZE = 16;

    public Canvas(EnchantingTableScreen screen) {
        this.screen = screen;

        setSize(32);
    }

    public EnchantingTableScreen screen() {
        return screen;
    }

    public void render(GuiGraphicsExtractor graphics) {
        float viewportLeft = screen().camera().VIEWPORT_X;
        float viewportTop = screen().camera().VIEWPORT_Y;
        float viewportRight = viewportLeft + screen().camera().VIEWPORT_WIDTH;
        float viewportBottom = viewportTop + screen().camera().VIEWPORT_HEIGHT;

        for (int x = 0; x < backgroundTileCount; x++) {
            for (int y = 0; y < backgroundTileCount; y++) {

                //Viewport culling for tiles
                float canvasX = x * TILE_SIZE;
                float canvasY = y * TILE_SIZE;
                Vector2f screenPos = canvasToScreen(canvasX, canvasY);

                float size = getScaledLength(TILE_SIZE);
                float left = screenPos.x();
                float top = screenPos.y();
                float right = left + size;
                float bottom = top + size;

                if(right < viewportLeft || left > viewportRight
                        || bottom < viewportTop || top > viewportBottom) continue;

                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        Sprite.BACKGROUND_TILE.id(),
                        x * TILE_SIZE, y * TILE_SIZE,
                        0, 0,
                        Sprite.BACKGROUND_TILE.width(), Sprite.BACKGROUND_TILE.height(),
                        Sprite.BACKGROUND_TILE.width(), Sprite.BACKGROUND_TILE.height()
                );
            }
        }
    }

    public void setSizeToFitNodes(int highestEnchantmentLevel) {
        int tileCount = ((BranchManager.getNodeStep() * 2) * highestEnchantmentLevel + TILE_SIZE - 1) / TILE_SIZE;
        int tileMargin = 2;
        setSize(tileCount + tileMargin);
    }

    public void setSize(int tileCount) {
        this.backgroundTileCount = tileCount;
        this.width = backgroundTileCount * TILE_SIZE;
        this.height = backgroundTileCount * TILE_SIZE;
    }

    public int getSize() {
        return backgroundTileCount * TILE_SIZE;
    }

    public Vector2i getCenter() {
        return new Vector2i(width / 2, height / 2);
    }

    public Vector2f canvasToScreen(Vector2f pos) {
        return new Vector2f(
                (pos.x() - screen().camera().x()) * screen().camera().zoom() + screen().camera().VIEWPORT_X,
                (pos.y() - screen().camera().y()) * screen().camera().zoom() + screen().camera().VIEWPORT_Y
        );
    }

    public Vector2f canvasToScreen(float x, float y) {
        return canvasToScreen(new Vector2f(x, y));
    }

    public Vector2f screenToCanvas(Vector2f pos) {
        return new Vector2f(
                (pos.x() - screen().camera().VIEWPORT_X) / screen().camera().zoom() + screen().camera().x(),
                (pos.y() - screen().camera().VIEWPORT_Y) / screen().camera().zoom() + screen().camera().y()
        );
    }

    public Vector2f screenToCanvas(float x, float y) {
        return screenToCanvas(new Vector2f(x, y));
    }

    private float getScaledLength(float length) {
        return length * screen().camera().zoom();
    }

    /**
     * Returns true if the mouse is over the specified canvas coordinates.
     * @param canvasX
     * @param canvasY
     * @param width
     * @param height
     * @param mouseX
     * @param mouseY
     * @return
     */
    public boolean isMouseOver(float canvasX, float canvasY,
                               float width, float height,
                               double mouseX, double mouseY) {
        Vector2f screenPos = canvasToScreen(canvasX, canvasY);
        float scaledWidth = getScaledLength(width);
        float scaledHeight = getScaledLength(height);

        float left = screenPos.x();
        float top = screenPos.y();
        float right = left + scaledWidth;
        float bottom = top + scaledHeight;

        float viewportLeft = screen().camera().VIEWPORT_X;
        float viewportTop = screen().camera().VIEWPORT_Y;
        float viewportRight = viewportLeft + screen().camera().VIEWPORT_WIDTH;
        float viewportBottom = viewportTop + screen().camera().VIEWPORT_HEIGHT;

        float visibleLeft = Math.max(left, viewportLeft);
        float visibleTop = Math.max(top, viewportTop);
        float visibleRight = Math.min(right, viewportRight);
        float visibleBottom = Math.min(bottom, viewportBottom);

        if(visibleLeft >= visibleRight || visibleTop >= visibleBottom) return false;

        return mouseX >= visibleLeft && mouseX <= visibleRight
                && mouseY >= visibleTop && mouseY <= visibleBottom;
    }
}
