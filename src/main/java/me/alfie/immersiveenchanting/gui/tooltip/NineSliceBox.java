package me.alfie.immersiveenchanting.gui.tooltip;

import me.alfie.immersiveenchanting.gui.EnchantingNodeTooltip;
import me.alfie.immersiveenchanting.gui.NodeTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Draw nine sliced sprites in EnchantingNodeTooltips. Shared by TooltipTitle/TooltipDescription
 * Override draw() to add rendering logic, like strings, items etc.
 */
public class NineSliceBox {

    private int boxWidth = 16; //Size of sprite (Base width of 16 as 0 is invisible)
    private int boxHeight = 16; //Size of sprite
    private ResourceLocation spriteTexture;
    private int x;
    private int y;
    public NodeTooltip parentTooltip;

    public NineSliceBox(NodeTooltip parentTooltip, ResourceLocation spriteTexture) {
        this.parentTooltip = parentTooltip;
        this.spriteTexture = spriteTexture;
    }

    /**
     * Draw a nine-sliced sprite. Use @Override for custom rendering.
     * @param graphics
     */
    public void draw(GuiGraphics graphics) {
        //Draw nine-sliced
        graphics.blitSprite(spriteTexture, x, y, 0, boxWidth, boxHeight);
    }

    /**
     * Helper to set pos first then draw. For custom rendering use draw(GuiGraphics graphics)
     * @param graphics
     * @param x
     * @param y
     */
    public void draw(GuiGraphics graphics, int x, int y) {
        setPos(x, y);
        draw(graphics);
    }

    public final void setSpriteTexture(ResourceLocation spriteTexture) {
        this.spriteTexture = spriteTexture;
    }

    public final void setBoxSize(int x, int y) {
        boxWidth = x;
        boxHeight = y;
    }

    public final void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    public final int getBoxWidth() {
        return boxWidth;
    }

    public final int getBoxHeight() {
        return boxHeight;
    }
}
