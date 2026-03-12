package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.core.ScrollableCanvas;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

/**
 * Generic class for all node elements on the enchanting table screen.<br>
 * Nodes can have 2 states: obtained or unobtained. Textures will be rendered automatically.
 */
public abstract class Node {

    private int x, y;
    static final int width = 26; //Texture size
    static final int height = 26; //Texture size
    private float scale = 1;

    private boolean obtained;

    protected ResourceLocation iconTexture;
    protected NodeType nodeType;

    public Node(NodeType nodeType, ResourceLocation iconTexture) {
        this.nodeType = nodeType;
        this.iconTexture = iconTexture;
    }

    public void render(GuiGraphics guiGraphics, ScrollableCanvas canvas) {
        guiGraphics.pose().pushPose();

        guiGraphics.pose().translate(this.x - (int) canvas.getScrollX(), this.y - (int) canvas.getScrollY(), 0);
        guiGraphics.pose().scale(scale, scale, scale);

        guiGraphics.blit(
                this.getCurrentTexture(),
                0,
                0,
                0f, 0f,
                width, height,
                width, height
        );

        //Blit book texture
        if (iconTexture != null) {
            guiGraphics.blit(
                    iconTexture,
                    4,
                    4,
                    0f, 0f,
                    16, 16,
                    16, 16
            );
        }
        guiGraphics.pose().popPose();

    }

    protected void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    protected void setIconTexture(ResourceLocation iconTexture) {
        this.iconTexture = iconTexture;
    }

    public ResourceLocation getCurrentTexture() {
        return obtained ? nodeType.getObtainedTexture() : nodeType.getUnobtainedTexture();
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isInViewport(double scrollX, double scrollY, int viewportWidth, int viewportHeight) {
        return getX() + width >= scrollX &&
                getX() <= scrollX + viewportWidth &&
                getY() + height >= scrollY &&
                getY() <= scrollY + viewportHeight;
    }

    public void setObtained(boolean obtained) {
        this.obtained = obtained;
    }

    public boolean isObtained() {
        return this.obtained;
    }

    public Vector2i getRenderedPosition(ScrollableCanvas canvas) {
        int renderedX = this.x - (int) canvas.getScrollX();
        int renderedY = this.y - (int) canvas.getScrollY();
        return new Vector2i(renderedX, renderedY);
    }

    public Vector2i getViewportPosition(ScrollableCanvas canvas) {
        int screenX = this.x - (int) canvas.getScrollX();
        int screenY = this.y - (int) canvas.getScrollY();

        int viewportX = screenX - canvas.getCanvasLeftPos();
        int viewportY = screenY - canvas.getCanvasTopPos();

        return new Vector2i(viewportX, viewportY);
    }

    /**
     * Helper function to check if a node is being moused over.
     * @param canvas
     * @param mouseX
     * @param mouseY
     * @return
     */
    public boolean isMouseOver(ScrollableCanvas canvas, double mouseX, double mouseY) {
        return canvas.isMouseOverBoundingBox(new Vector2i(getX(), getY()),
                width, height, mouseX, mouseY);
    }

    public abstract boolean onClicked(int mouseX, int mouseY);
}
