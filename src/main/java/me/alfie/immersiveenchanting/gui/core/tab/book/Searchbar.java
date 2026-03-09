package me.alfie.immersiveenchanting.gui.core.tab.book;

import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Searchbar {

    BookTab bookTab;

    private final StringBuilder searchString = new StringBuilder();
    private int caretPosition = 0;
    private long lastCaretTime = 0;
    private boolean caretVisible;

    public Searchbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

    public void render(GuiGraphics guiGraphics) {
        int x = bookTab.screen.getGuiLeft() + 138;
        int y = bookTab.screen.getGuiTop() + 100;

        guiGraphics.blit(
                Sprite.SEARCH.get(),
                x,
                y,
                0f, 0f, 100, 20,
                100, 20
        );

        final int textPadding = 4;
        guiGraphics.drawString(bookTab.screen.getMinecraft().font,
                searchString.toString(),
                x+textPadding, y+textPadding+2, Color.WHITE.hashCode());
        renderCaret(guiGraphics, x+textPadding, y+textPadding+2);
    }

    private void renderCaret(GuiGraphics guiGraphics, int x, int y) {
        long currentTime = System.currentTimeMillis();
        final int caretSpeed = 500;
        if(currentTime - lastCaretTime > caretSpeed) {
            caretVisible = !caretVisible;
            lastCaretTime = currentTime;
        }

        if(caretVisible) {
            int textWidth = bookTab.screen.getMinecraft().font.width(searchString.toString());
            guiGraphics.drawString(bookTab.screen.getMinecraft().font,
                    "_", x + textWidth, y, Color.WHITE.hashCode());
        }
    }

    public void addCharToSearch(char codePoint) {
        searchString.append(codePoint);
        caretPosition++;
        bookTab.scrollbar.resetScrollIndex();
    }

    public void removeCharFromSearch() {
        if(!searchString.isEmpty()) {
            searchString.deleteCharAt(caretPosition-1);
            caretPosition--;
            if(caretPosition < 0) caretPosition = 0;
            bookTab.scrollbar.resetScrollIndex();
        }
    }

    /**
     * Filters the book tab's enchantments based on search string.
     */
    protected void checkSearch() {
        List<Holder<Enchantment>> toRemove = new ArrayList<>();

        for (Holder<Enchantment> enchantmentHolder : bookTab.getEnchantments()) {
            String enchantmentName = enchantmentHolder.value().description().getString();

            // If the enchantment doesn't match the search query, add it to the remove list
            if (!enchantmentName.toLowerCase().contains(searchString.toString().toLowerCase())) {
                toRemove.add(enchantmentHolder);
            }
        }

        // Remove the collected enchantments after the loop finishes
        bookTab.getEnchantments().removeAll(toRemove);
    }

    public void clearSearch() {
        searchString.setLength(0);
    }
}
