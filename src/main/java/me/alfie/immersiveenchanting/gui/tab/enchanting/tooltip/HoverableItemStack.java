package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

/**
 * A lightweight UI helper for rendering an {@link ItemStack} at a fixed screen position
 * with built-in hover tooltip support.
 *
 * <p><strong>Important:</strong> This class is designed for <b>screen-space rendering only</b>.
 * It should NOT be used for objects rendered inside a transformed canvas (e.g. zoomed or
 * translated via a camera), as hover detection assumes raw screen coordinates.
 *
 * <p>When the mouse is positioned over the item (16x16 area), the item's tooltip
 * (decorations) will be rendered at the cursor position.
 */
public class HoverableItemStack {

    private float x;
    private float y;

    private final EnchantingTableScreen screen;
    private final ItemStack stack;

    /**
     * Creates a new hoverable item stack renderer.
     *
     * @param screen the screen used for mouse position and hover checks
     * @param stack the item stack to render
     */
    public HoverableItemStack(EnchantingTableScreen screen, ItemStack stack) {
        this.screen = screen;
        this.stack = stack;
    }

    /**
     * Renders the item stack and, if hovered, its tooltip.
     *
     * <p>The item is rendered at its assigned (x, y) position. If the mouse is within
     * the item's 16x16 bounds, the item's tooltip is rendered at the mouse cursor.
     *
     * @param graphics the rendering context
     * @param mouseX current mouse X position (screen space)
     * @param mouseY current mouse Y position (screen space)
     */
    public void render(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        graphics.item(stack, (int) x, (int) y);
        graphics.itemDecorations(Minecraft.getInstance().font, stack, (int) x, (int) y);

        if(screen.isMouseOver(x, y, 16, 16, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, stack, (int) mouseX, (int) mouseY);
        }
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
