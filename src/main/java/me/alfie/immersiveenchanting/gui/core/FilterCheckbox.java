package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class FilterCheckbox {

    boolean enabled = true;

    public final BookFilters filterType;
    public final EnchantingTableScreen screen;

    private int x;
    private int y;

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

    public FilterCheckbox(EnchantingTableScreen enchantingTableScreen, BookFilters filter) {
        filterType = filter;
        screen = enchantingTableScreen;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggleEnabled() {
        this.enabled = !this.enabled;
        screen.bookWindow.resetScrollIndex();
    }

    public void render(GuiGraphics guiGraphics) {
        final int spriteSize = 18;
        Sprite sprite = isEnabled() ? Sprite.CHECKBOX_ON : Sprite.CHECKBOX_OFF;

        int x = screen.getGuiLeft() + this.x;
        int y = screen.getGuiTop() + this.y;
        guiGraphics.blit(
                sprite.get(),
                x,
                y,
                0f, 0f, spriteSize, spriteSize,
                spriteSize, spriteSize
        );

        guiGraphics.drawString(screen.getMinecraft().font, filterType.getLabel(),
                x+20, y, Color.WHITE.hashCode());
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return screen.isMouseOver(mouseX, mouseY, screen.getGuiLeft() + x, screen.getGuiTop() + y, 18, 18);
    }
}
