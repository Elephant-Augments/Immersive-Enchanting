package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.*;

public class FilterCheckbox implements ScreenEventListener {

    private boolean enabled = true;
    protected final BookFilters filterType;
    private final BookTab bookTab;

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
        bookTab.scrollbar().resetScrollIndex();
    }

    public void render(GuiGraphics graphics, double mouseX, double mouseY) {
        Sprite sprite = isEnabled() ? Sprite.CHECKBOX_ON : Sprite.CHECKBOX_OFF;

        int x = bookTab.screen().getGuiLeft() + this.x;
        int y = bookTab.screen().getGuiTop() + this.y;
        graphics.blit(
                sprite.id(),
                x,
                y,
                0f, 0f,
                sprite.width(), sprite.height(),
                sprite.width(), sprite.height()
        );

        graphics.drawString(Minecraft.getInstance().font, filterType.getLabel(), x+20, y, Color.WHITE.getRGB());
    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        return bookTab.screen().isMouseOver(
                bookTab.screen().getGuiLeft() + x, bookTab.screen().getGuiTop() + y,
                Sprite.CHECKBOX_ON.width(), Sprite.CHECKBOX_ON.height(),
                mouseX, mouseY);
    }

    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if(this.isMouseOver(mouseX, mouseY)) {
            FxHelper.playGenericUISound(bookTab.screen().player());
            toggleEnabled();
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouseX, mouseY, button);
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
