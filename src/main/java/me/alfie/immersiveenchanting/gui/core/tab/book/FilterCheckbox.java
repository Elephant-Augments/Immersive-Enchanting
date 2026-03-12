package me.alfie.immersiveenchanting.gui.core.tab.book;

import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class FilterCheckbox {

    boolean enabled = true;

    public final BookFilters filterType;
    public final BookTab bookTab;

    private int x;
    private int y;

    public FilterCheckbox(BookTab bookTab, BookFilters filter) {
        filterType = filter;
        this.bookTab = bookTab;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggleEnabled() {
        this.enabled = !this.enabled;
        bookTab.scrollbar.resetScrollIndex();
    }

    public void render(GuiGraphics guiGraphics) {
        final int spriteSize = 18;
        Sprite sprite = isEnabled() ? Sprite.CHECKBOX_ON : Sprite.CHECKBOX_OFF;

        int x = bookTab.screen.getGuiLeft() + this.x;
        int y = bookTab.screen.getGuiTop() + this.y;
        guiGraphics.blit(
                sprite.get(),
                x,
                y,
                0f, 0f, spriteSize, spriteSize,
                spriteSize, spriteSize
        );

        guiGraphics.drawString(bookTab.screen.getMinecraft().font, filterType.getLabel(),
                x+20, y, Color.WHITE.hashCode());
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return bookTab.screen.isMouseOver(mouseX, mouseY,
                bookTab.screen.getGuiLeft() + x,
                bookTab.screen.getGuiTop() + y, 18, 18);
    }

    public boolean onMouseClick(int mouseX, int mouseY) {
        if(isMouseOver(mouseX, mouseY)) {
            FxHelper.playGenericUISound(bookTab.screen.player);
            toggleEnabled();
            return true;
        }
        return false;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
