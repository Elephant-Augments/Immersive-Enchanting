package me.alfie.immersiveenchanting.gui.core;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeBranch;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Vector2i;

/**
 * A canvas that can be panned with the mouse.
 */
public class ScrollableCanvas {
    public final int TILE_TEXTURE_SIZE = 16;
    int width = TILE_TEXTURE_SIZE * 64; //Must be divisible by tileSize (16), otherwise rendered tiles/edge constraints will leave gaps
    int height = TILE_TEXTURE_SIZE * 64; //Must be divisible by tileSize (16), otherwise rendered tiles/edge constraints will leave gaps

    int canvasLeftPos;
    int canvasTopPos;

    double scrollX = 0;
    double scrollY = 0;

    private final Vector2i VIEWPORT_TOP_LEFT = new Vector2i(5, 5); //Position that viewport starts on the texture (top left)
    public final int VIEWPORT_WIDTH = 247; //Dimensions of viewport in the texture
    public final int VIEWPORT_HEIGHT = 117; //Dimensions of viewport in the texture

    private boolean dragging = false;
    private double dragStartMouseX = 0;
    private double dragStartMouseY = 0;
    private double dragStartScrollX = 0;
    private double dragStartScrollY = 0;

    private boolean isDraggingEnabled = false;

    private final EnchantingTableScreen screen;

    public ScrollableCanvas(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCanvasLeftPos() {
        return canvasLeftPos;
    }

    public int getCanvasTopPos() {
        return canvasTopPos;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    public void setScrollX(double scrollX) {
        this.scrollX = scrollX;
    }

    public void setScrollY(double scrollY) {
        this.scrollY = scrollY;
    }

    /**
     * Calculate the smallest possible canvas size to fit the highest level of available enchantment.
     */
    public void calculateSize() {
        //Setting size dynamically requires recentering each time.

        //Each NODE_STEP = 80 pixels, as canvas is centred 80 pixels * 2 reaches the centre of the first node
        int pixels = (NodeBranch.node_step * 2) * EnchantmentCostRegistry.getClientRegistry().getHighestEnchantmentLevel();
        int margin = TILE_TEXTURE_SIZE * 4; //Add 4 tile margin
        int rounded = ((pixels + TILE_TEXTURE_SIZE - 1) / TILE_TEXTURE_SIZE) * TILE_TEXTURE_SIZE;

        width = rounded + margin;
        height = rounded + margin;

        scrollX = (width / 2.0) - (VIEWPORT_WIDTH / 2.0);
        scrollY = (height / 2.0) - (VIEWPORT_HEIGHT / 2.0);

        //These have to be updated here, idk why but it just breaks ok?
        canvasLeftPos = screen.getGuiLeft() + VIEWPORT_TOP_LEFT.x;
        canvasTopPos = screen.getGuiTop() + VIEWPORT_TOP_LEFT.y;
    }

    /**
     * Render a tiled background and set up viewport culling.
     *
     * @param guiGraphics
     */
    private void renderTiledBg(GuiGraphics guiGraphics, Sprite sprite) {
        //Setup viewport culling
        int viewportLeft = screen.getGuiLeft() + VIEWPORT_TOP_LEFT.x;
        int viewportTop = screen.getGuiTop() + VIEWPORT_TOP_LEFT.y;
        int viewportRight = viewportLeft + VIEWPORT_WIDTH;
        int viewportBottom = viewportTop + VIEWPORT_HEIGHT;

        double scale = screen.getMinecraft().getWindow().getGuiScale();
        RenderSystem.enableScissor(
                (int) (viewportLeft * scale),
                (int) ((screen.height - viewportBottom) * scale), // Y is flipped!
                (int) (VIEWPORT_WIDTH * scale),
                (int) (VIEWPORT_HEIGHT * scale)
        );

        //Draw a tiled background using TILE_TEXTURE as a background
        int numberOfTilesX = (width / TILE_TEXTURE_SIZE);
        int numberOfTilesY = (height / TILE_TEXTURE_SIZE);

        for (int x = 0; x < numberOfTilesX; x++) {
            for (int y = 0; y < numberOfTilesY; y++) {
                // Calculate tile screen position
                int tileScreenX = canvasLeftPos + x * TILE_TEXTURE_SIZE - (int) scrollX;
                int tileScreenY = canvasTopPos + y * TILE_TEXTURE_SIZE - (int) scrollY;

                // Skip tiles completely outside the viewport
                if (tileScreenX + TILE_TEXTURE_SIZE < viewportLeft || tileScreenX > viewportRight ||
                        tileScreenY + TILE_TEXTURE_SIZE < viewportTop || tileScreenY > viewportBottom) {
                    continue;
                }

                guiGraphics.blit(
                        sprite.get(),
                        canvasLeftPos + x * TILE_TEXTURE_SIZE - (int) scrollX,
                        canvasTopPos + y * TILE_TEXTURE_SIZE - (int) scrollY,
                        0f, 0f,
                        TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE,
                        TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE
                );
            }
        }
        //Reset brightness
        guiGraphics.setColor(1f, 1f, 1f, 1f);
    }

    public void renderTiledBg(GuiGraphics guiGraphics) {
        if(screen.getState().equals(ScreenState.ENCHANTING)) {
            renderTiledBg(guiGraphics, Sprite.ENCHANTING_TILE);
        } else if (screen.getState().equals(ScreenState.BOOKS)) {
            renderTiledBg(guiGraphics, Sprite.ENCHANTING_TILE);
        }
    }

    /**
     * Helper function to get the coordinate to center an object on the canvas.
     *
     * @param textureWidth
     * @param textureHeight
     * @return
     */
    public Vector2i getCenterPos(int textureWidth, int textureHeight) {
        int x = canvasLeftPos + width / 2 - textureWidth / 2;
        int y = canvasTopPos + height / 2 - textureHeight / 2;
        return new Vector2i(x, y);
    }

    /**
     * Check if mouse is over a bounding box. Automatically clips the bounding box if out of viewport bounds.
     *
     * @param topLeftPos Top Left position of the bounding box to detect
     * @param width      Width of the bounding box to detect
     * @param height     Height of the bounding box to detect
     * @param mouseX
     * @param mouseY
     * @return
     */
    public boolean isMouseOverBoundingBox(
            Vector2i topLeftPos,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        // Node's position on screen
        int drawX = topLeftPos.x - (int) scrollX;
        int drawY = topLeftPos.y - (int) scrollY;

        // Node bounds
        int boxLeft = drawX;
        int boxTop = drawY;
        int boxRight = drawX + width;
        int boxBottom = drawY + height;

        // Viewport bounds
        int viewportLeft = canvasLeftPos;
        int viewportTop = canvasTopPos;
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

    public boolean startDrag(double mouseX, double mouseY) {
        // Compute viewport bounds in screen coordinates
        int viewportLeft = screen.getGuiLeft() + VIEWPORT_TOP_LEFT.x;
        int viewportTop = screen.getGuiTop() + VIEWPORT_TOP_LEFT.y;
        int viewportRight = viewportLeft + VIEWPORT_WIDTH;
        int viewportBottom = viewportTop + VIEWPORT_HEIGHT;

        // Only start dragging if the mouse is inside the viewport
        if (mouseX >= viewportLeft && mouseX < viewportRight &&
                mouseY >= viewportTop && mouseY < viewportBottom) {
            dragging = true;
            dragStartMouseX = mouseX;
            dragStartMouseY = mouseY;
            dragStartScrollX = scrollX;
            dragStartScrollY = scrollY;
            return true; // consume the click
        }
        return false;
    }

    public boolean drag(double mouseX, double mouseY, int button) {
        if (dragging && button == 0) {
            scrollX = dragStartScrollX + (dragStartMouseX - mouseX);
            scrollY = dragStartScrollY + (dragStartMouseY - mouseY);

            //Left Edge
            if (scrollX + canvasLeftPos < canvasLeftPos) {
                scrollX = 0;
            }

            //Top Edge
            if (scrollY + canvasTopPos < canvasTopPos) {
                scrollY = 0;
            }

            // Right edge
            if (scrollX > width - VIEWPORT_WIDTH) {
                scrollX = width - VIEWPORT_WIDTH;
            }

            // Bottom edge
            if (scrollY > height - VIEWPORT_HEIGHT) {
                scrollY = height - VIEWPORT_HEIGHT;
            }
            return true;
        }
        return false;
    }

    public boolean stopDrag(int button) {
        if (button == 0) dragging = false;
        return dragging;
    }

    public boolean isDraggingEnabled() {
        return isDraggingEnabled;
    }

    public void setDraggingEnabled(boolean draggingEnabled) {
        isDraggingEnabled = draggingEnabled;
    }


}
