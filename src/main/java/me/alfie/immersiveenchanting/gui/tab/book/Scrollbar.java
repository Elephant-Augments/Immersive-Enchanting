package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class Scrollbar implements ScreenEventListener {

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
                Sprite.SCROLLER.width(),  Sprite.SCROLLER.height(),
                Sprite.SCROLLER.width(),  Sprite.SCROLLER.height()
        );

    }

    /**
     * Get the amount of offset for the scroller based on scroll index.
     * @return
     */
    private int getScrollwheelOffset() {
        int trackHeight = scrollbarHeight - scrollerHeight - 2;

        int totalBoxes = bookTab.getRenderedEnchantments().size();
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

            int maxIndex = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
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

        if(scrollIndex > bookTab.getRenderedEnchantments().size()- bookTab.MAX_BOXES_RENDERED)
            scrollIndex = bookTab.getRenderedEnchantments().size()- bookTab.MAX_BOXES_RENDERED;

        if(scrollIndex < 0) scrollIndex = 0;
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        return bookTab.screen().isMouseOver(
                barX, barY,
                Sprite.SCROLLBAR.width(), (Sprite.SCROLLBAR.height()),
                mouseX, mouseY);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if(isMouseOver(mouseX, mouseY)) {
            isMouseDraggingScroller = true;
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouseX, mouseY, button);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY) {
        scrollIndex -= (int) scrollY;

        int max = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
        scrollIndex = Mth.clamp(scrollIndex, 0, max);

        if(scrollIndex < 0) scrollIndex = 0;

        return ScreenEventListener.super.onMouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean onMouseDrag(double mouseX, double mouseY, int button, double dx, double dy) {
        if(isMouseDraggingScroller) {
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

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int button) {
        isMouseDraggingScroller = false;
        return ScreenEventListener.super.onMouseRelease(mouseX, mouseY, button);
    }
}
