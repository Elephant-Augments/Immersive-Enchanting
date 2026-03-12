package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.tooltip;

import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
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
    public void draw(GuiGraphics graphics, int uvOffset) {
        //Draw nine-sliced
        final int textureWidth = 200;
        final int textureHeight = 26;
        final int textureBorder = 10;
        graphics.blitNineSliced(spriteTexture, x, y, boxWidth, boxHeight, textureBorder, textureWidth, textureHeight, 0, uvOffset);
    }

    /**
     * Helper to set pos first then draw. For custom rendering use draw(GuiGraphics graphics)
     * @param graphics
     * @param x
     * @param y
     */
    public void draw(GuiGraphics graphics, int x, int y, int uvOffset) {
        setPos(x, y);
        draw(graphics, uvOffset);
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
