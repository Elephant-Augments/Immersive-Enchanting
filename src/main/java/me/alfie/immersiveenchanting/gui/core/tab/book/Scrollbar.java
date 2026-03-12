package me.alfie.immersiveenchanting.gui.core.tab.book;

import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphics;

public class Scrollbar {

    public final BookTab bookTab;

    private final int scrollerHeight = 15;

    private final int scrollbarWidth = 14;
    private final int scrollbarHeight = 114;

    protected int scrollIndex = 0;

    private int barX;
    private int barY;

    private int scrollerX;
    private int scrollerY;
    public boolean isMouseDraggingScroller;

    public Scrollbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

    public void render(GuiGraphics guiGraphics) {
        barX = bookTab.screen.getGuiLeft() + 121;
        barY = bookTab.screen.getGuiTop() + 6;

        guiGraphics.blit(
                Sprite.SCROLLBAR.get(),
                barX,
                barY,
                0f, 0f, scrollbarWidth, scrollbarHeight,
                scrollbarWidth, scrollbarHeight
        );

        int scrollerOffset = getScrollwheelOffset();
        scrollerY = barY + 1 + scrollerOffset;
        scrollerX = barX+1;


        guiGraphics.blit(
                Sprite.SCROLLER.get(),
                scrollerX,
                scrollerY,
                0f, 0f, 12, scrollerHeight,
                12, scrollerHeight
        );
    }

    /**
     * Get the amount of offset for the scroller based on scroll index.
     * @return
     */
    private int getScrollwheelOffset() {
        int trackHeight = scrollbarHeight - scrollerHeight - 2;

        int totalBoxes = bookTab.getEnchantments().size();
        int maxScrollIndex = Math.max(0, totalBoxes - bookTab.MAX_BOXES_RENDERED);

        if (maxScrollIndex == 0) return 0;

        double percent = (double) scrollIndex / maxScrollIndex;
        return (int) Math.round(percent * trackHeight);
    }

    public boolean updateScrollFromMouse(int mouseY) {
        if(isMouseDraggingScroller) {
            int minY = barY + 1;
            int maxY = barY + scrollbarHeight - scrollerHeight - 1;

            int clamped = Math.max(minY, Math.min(mouseY - scrollerHeight / 2, maxY));

            double percent = (double)(clamped - minY) / (maxY - minY);

            int maxIndex = Math.max(0, bookTab.getEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
            scrollIndex = (int)Math.round(percent * maxIndex);
            return true;
        }
        return false;
    }

    public void resetScrollIndex() {
        scrollIndex = 0;
    }

    public void incrementScrollIndex(int increment) {
        scrollIndex += increment;

        if(scrollIndex > bookTab.getEnchantments().size()- bookTab.MAX_BOXES_RENDERED)
            scrollIndex = bookTab.getEnchantments().size()- bookTab.MAX_BOXES_RENDERED;

        if(scrollIndex < 0) scrollIndex = 0;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return bookTab.screen.isMouseOver(
                mouseX, mouseY,
                barX, barY,
                scrollbarWidth, scrollbarHeight);
    }

    public boolean onMouseClick(int mouseX, int mouseY) {
        if(isMouseOver(mouseX, mouseY)) {
            isMouseDraggingScroller = true;
            return true;
        }
        return false;
    }
}
