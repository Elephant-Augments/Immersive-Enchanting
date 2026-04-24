package me.alfie.immersiveenchanting.gui.tab.book;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

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

    public void render(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        barX = bookTab.screen().getGuiLeft() + 121;
        barY = bookTab.screen().getGuiTop() + 6;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
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
                RenderPipelines.GUI_TEXTURED,
                Sprite.SCROLLER.id(),
                scrollerX,
                scrollerY,
                0f, 0f,
                Sprite.SCROLLER.width(),  Sprite.SCROLLER.height(),
                Sprite.SCROLLER.width(),  Sprite.SCROLLER.height()
        );

        if(this.isMouseOver(mouseX, mouseY)) graphics.requestCursor(CursorTypes.POINTING_HAND);
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
    public boolean onMouseClick(MouseButtonEvent mouse) {
        if(isMouseOver(mouse.x(), mouse.y())) {
            isMouseDraggingScroller = true;
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouse);
    }

    @Override
    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollY) {
        scrollIndex -= (int) scrollY;

        int max = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
        scrollIndex = Math.clamp(scrollIndex, 0, max);

        if(scrollIndex < 0) scrollIndex = 0;

        return ScreenEventListener.super.onMouseScrolled(mouseX, mouseY, scrollY);
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent mouse, double dx, double dy) {
        if(isMouseDraggingScroller) {
            int minY = barY + 1;
            int maxY = barY + scrollbarHeight - scrollerHeight - 1;

            int clamped = Math.max(minY, Math.min((int) mouse.y() - scrollerHeight / 2, maxY));

            double percent = (double) (clamped - minY) / (maxY - minY);

            int maxIndex = Math.max(0, bookTab.getRenderedEnchantments().size() - bookTab.MAX_BOXES_RENDERED);
            scrollIndex = (int) Math.round(percent * maxIndex);
            return true;
        }

        return ScreenEventListener.super.onMouseDrag(mouse, dx, dy);
    }

    @Override
    public boolean onMouseRelease(MouseButtonEvent mouse) {
        isMouseDraggingScroller = false;
        return ScreenEventListener.super.onMouseRelease(mouse);
    }
}
