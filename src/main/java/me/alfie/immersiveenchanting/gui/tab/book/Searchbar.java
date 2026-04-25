package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A text input component used in the {@link BookTab} to filter enchantments.
 *
 * <p>This search bar allows players to dynamically filter the visible
 * enchantments list by typing part of an enchantment's name.</p>
 *
 * <p>Features:
 * <ul>
 *     <li>Real-time filtering of enchantments</li>
 *     <li>Blinking caret indicator</li>
 *     <li>Automatic scrollbar reset on input changes</li>
 * </ul>
 *
 * <p>The filtering logic is applied via {@link #checkSearch()}, which removes
 * non-matching enchantments from the rendered list.</p>
 */
public class Searchbar {

    BookTab bookTab;

    private final StringBuilder searchString = new StringBuilder();
    private int caretPosition = 0;
    private long lastCaretTime = 0;
    private boolean caretVisible;

    public Searchbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

    /**
     * Renders the search bar background, current input text, and caret.
     *
     * @param graphics Rendering context
     */
    public void render(GuiGraphicsExtractor graphics) {
        int xPos = bookTab.screen().getGuiLeft() + 138;
        int yPos = bookTab.screen().getGuiTop() + 100;

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                Sprite.SEARCH.id(),
                xPos, yPos,
                0f, 0f,
                Sprite.SEARCH.width(), Sprite.SEARCH.height(),
                Sprite.SEARCH.width(), Sprite.SEARCH.height()
        );

        final int padding = 4;
        graphics.text(Minecraft.getInstance().font,
                searchString.toString(),
                xPos + padding, yPos + padding + 2, Color.WHITE.hashCode());

        renderCaret(graphics, xPos + padding, yPos + padding + 2);
    }

    /**
     * Renders a blinking caret at the end of the current search string.
     *
     * <p>The caret visibility toggles at a fixed interval to simulate
     * a standard text cursor.</p>
     *
     * @param graphics Rendering context
     * @param x        Base X position of the text
     * @param y        Base Y position of the text
     */
    private void renderCaret(GuiGraphicsExtractor graphics, int x, int y) {
        long currentTime = System.currentTimeMillis();
        final int caretSpeed = 500;
        if(currentTime - lastCaretTime > caretSpeed) {
            caretVisible = !caretVisible;
            lastCaretTime = currentTime;
        }

        if(caretVisible) {
            int textWidth = Minecraft.getInstance().font.width(searchString.toString());
            graphics.text(Minecraft.getInstance().font, "_", x + textWidth, y, Color.WHITE.hashCode());
        }
    }

    /**
     * Appends a character to the search string.
     *
     * <p>Also advances the caret position and resets the scrollbar
     * to ensure filtered results are visible from the top.</p>
     *
     * @param codePoint Character to append
     */
    public void addCharToSearch(char codePoint) {
        searchString.append(codePoint);
        caretPosition++;
        bookTab.scrollbar().resetScrollIndex();
    }

    /**
     * Removes the last character from the search string.
     *
     * <p>Safely handles empty strings and ensures the caret position
     * does not become negative. Also resets the scrollbar.</p>
     */
    public void removeCharFromSearch() {
        if(!searchString.isEmpty()) {
            searchString.deleteCharAt(caretPosition-1);
            caretPosition--;
            if(caretPosition < 0) caretPosition = 0;
            bookTab.scrollbar().resetScrollIndex();
        }
    }

    /**
     * Filters the {@link BookTab}'s rendered enchantments based on the current search string.
     *
     * <p>Any enchantment whose name does not contain the search string
     * (case-insensitive) is removed from the rendered list.</p>
     *
     * <p>This method mutates the list returned by
     * {@link BookTab#getRenderedEnchantments()}.</p>
     */
    protected void checkSearch() {
        List<Holder<Enchantment>> toRemove = new ArrayList<>();

        for (Holder<Enchantment> enchantmentHolder : bookTab.getRenderedEnchantments()) {
            String enchantmentName = enchantmentHolder.value().description().getString();

            if (!enchantmentName.toLowerCase().contains(searchString.toString().toLowerCase())) {
                toRemove.add(enchantmentHolder);
            }
        }

        bookTab.getRenderedEnchantments().removeAll(toRemove);
    }

    /**
     * Clears the current search string.
     *
     * <p>Resets the input to an empty state but does not automatically
     * reapply filters or reset scroll position.</p>
     */
    public void clearSearch() {
        searchString.setLength(0);
    }
}
