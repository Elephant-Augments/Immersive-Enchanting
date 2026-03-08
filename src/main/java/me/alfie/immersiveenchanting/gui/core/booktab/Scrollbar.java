package me.alfie.immersiveenchanting.gui.core.booktab;

import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphics;

public class Scrollbar {

    public final BookTab bookTab;

    private final int scrollerHeight = 15;
    private final int scrollbarHeight = 114;

    protected int scrollIndex = 0;

    public Scrollbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

    public void render(GuiGraphics guiGraphics) {
        int x = bookTab.screen.getGuiLeft() + 121;
        int y = bookTab.screen.getGuiTop() + 6;

        guiGraphics.blit(
                Sprite.SCROLLBAR.get(),
                x,
                y,
                0f, 0f, 14, scrollbarHeight,
                14, scrollbarHeight
        );

        int scrollerOffset = getScrollerOffset();

        guiGraphics.blit(
                Sprite.SCROLLER.get(),
                x+1,
                y+1+scrollerOffset,
                0f, 0f, 12, scrollerHeight,
                12, scrollerHeight
        );
    }

    /**
     * Get the amount of offset for the scroller.
     * @return
     */
    private int getScrollerOffset() {
        int bottom = scrollbarHeight - scrollerHeight -2;

        int totalEnchantments = bookTab.getEnchantments().size() + 1;
        double segmentHeight = (double) scrollbarHeight / totalEnchantments;
        double yOffset = scrollIndex * segmentHeight;

        // If we're at the last scroll index, force the scroller to the bottom of the scrollbar
        if (scrollIndex == bookTab.getEnchantments().size() - bookTab.MAX_BOXES_RENDERED) {
            yOffset = bottom;
        }
        int roundedYOffset = (int) Math.round(yOffset);
        return roundedYOffset;
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
}
