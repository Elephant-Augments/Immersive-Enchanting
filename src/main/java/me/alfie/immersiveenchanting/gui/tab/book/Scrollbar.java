package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A vertical scrollbar component used within the {@link BookTab}.
 *
 * <p>This scrollbar allows the player to navigate through a list of
 * enchantments when the total exceeds the visible limit.
 *
 * <p>Features:
 * <ul>
 *     <li>Mouse wheel scrolling</li>
 *     <li>Click-and-drag scroller interaction</li>
 *     <li>Scroll position synced to rendered enchantment list</li>
 * </ul>
 *
 * <p>The scroll position is represented by {@code scrollIndex}, which maps
 * directly to the starting index of visible enchantments.</p>
 */
public class Scrollbar implements ScreenEventListener {

    public final BookTab bookTab;

    private final int scrollerHeight = 15;

    private final int scrollbarWidth = 14;
    private final int scrollbarHeight = 114;
    public boolean isMouseDraggingScroller;
    protected int scrollIndex = 0;
    private int barX;
    private int barY;
    private int scrollerX;
    private int scrollerY;

    public Scrollbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

    /**
     * Renders the scrollbar track and draggable scroller.
     *
     * <p>Also updates internal positioning used for mouse interaction
     * and changes the cursor when hovered.</p>
     *
     * @param graphics Rendering context
     * @param mouseX   Current mouse X
     * @param mouseY   Current mouse Y
     */
    public void render(GuiGraphics graphics, double mouseX, double mouseY) {
        barX = bookTab.screen().getGuiLeft() + 121;
        barY = bookTab.screen().getGuiTop() + 6;

        graphics.blit(
                Sprite.SCROLLBAR.id(),
                barX,
                barY,
                0f, 0f,
                Sprite.SCROLLBAR.width(), Sprite.SCROLLBAR.height(),
                Sprite.SCROLLBAR.width(), Sprite.SCROLLBAR.height()
        );

        int scrollerOffset = getScrollwheelOffset();
        scrollerY = barY + 1 + scrollerOffset;
        scrollerX = barX + 1;


        graphics.blit(
                Sprite.SCROLLER.id(),
                scrollerX,
                scrollerY,
                0f, 0f,
                Sprite.SCROLLER.width(), Sprite.SCROLLER.height(),
                Sprite.SCROLLER.width(), Sprite.SCROLLER.height()
        );
    }

    /**
     * Calculates the vertical offset of the scroller within the scrollbar track.
     *
     * <p>The offset is determined by mapping {@code scrollIndex} to a percentage
     * of the total scrollable range.</p>
     *
     * @return Pixel offset from the top of the scrollbar track
     */
    private int getScrollwheelOffset() {
        int trackHeight = scrollbarHeight - scrollerHeight - 2;

        int totalBoxes = bookTab.getRenderedEnchantments().size();
        int maxScrollIndex = Math.max(0, totalBoxes - bookTab.MAX_BOXES_RENDERED);

        if (maxScrollIndex == 0) return 0;

        double percent = (double) scrollIndex / maxScrollIndex;
        return (int) Math.round(percent * trackHeight);
    }

    /**
     * Resets the scroll position to the top of the list.
     */
    public void resetScrollIndex() {
        scrollIndex = 0;
    }

    /**
     * Handles mouse click interaction.
     *
     * <p>If the scrollbar is clicked, dragging mode is enabled,
     * allowing the scroller to follow mouse movement.</p>
     *
     * @param mouse Mouse event
     * @return {@code true} if the click was handled
     */
    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            isMouseDraggingScroller = true;
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouseX, mouseY, button);
    }

    /**
     * Checks whether the mouse is hovering over the scrollbar track.
     *
     * @param mouseX Current mouse X
     * @param mouseY Current mouse Y
     * @return {@code true} if the mouse is within the scrollbar bounds
     */
    private boolean isMouseOver(double mouseX, double mouseY) {
        return bookTab.screen().isMouseOver(
                barX, barY,
                Sprite.SCROLLBAR.width(), (Sprite.SCROLLBAR.height()),
                mouseX, mouseY);
    }

    /**
     * Handles mouse dragging for the scroller.
     *
     * <p>When dragging is active, updates the scroll position
     * based on the mouse's vertical position.</p>
     *
     * @param mouse Mouse event
     * @param dx    Delta X
     * @param dy    Delta Y
     * @return {@code true} if dragging affected the scrollbar
     */
    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, int button, double dx, double dy) {
        if (isMouseDraggingScroller) {
            int minY = barY + 1;
            int maxY = barY + scrollbarHeight - scrollerHeight - 1;

            int clamped = Math.max(minY, Math.min((int) mouseY - scrollerHeight / 2, maxY));

            double percent = (double) (clamped - minY) / (maxY - minY);

            int maxIndex = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
            scrollIndex = (int) Math.round(percent * maxIndex);
            return true;
        }

        return ScreenEventListener.super.onMouseDrag(mouseX, mouseY, button, dx, dy);
    }

    /**
     * Handles mouse release events.
     *
     * <p>Disables dragging mode when the mouse button is released.</p>
     *
     * @param mouse Mouse event
     * @return {@code true} if handled
     */
    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int button) {
        isMouseDraggingScroller = false;
        return ScreenEventListener.super.onMouseRelease(mouseX, mouseY, button);
    }

    /**
     * Handles mouse wheel scrolling.
     *
     * <p>Adjusts {@code scrollIndex} based on scroll input and clamps
     * the result within valid bounds.</p>
     *
     * @param mouseX  Mouse X
     * @param mouseY  Mouse Y
     * @param scrollY Scroll delta
     * @return {@code true} if handled
     */
    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY) {
        scrollIndex -= (int) scrollY;

        int max = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
        scrollIndex = Math.clamp(scrollIndex, 0, max);

        if (scrollIndex < 0) scrollIndex = 0;

        return ScreenEventListener.super.onMouseScrolled(mouseX, mouseY, scrollY);
    }
}
