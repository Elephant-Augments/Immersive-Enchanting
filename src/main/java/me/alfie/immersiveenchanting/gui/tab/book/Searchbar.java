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

public class Searchbar {

    BookTab bookTab;

    private final StringBuilder searchString = new StringBuilder();
    private int caretPosition = 0;
    private long lastCaretTime = 0;
    private boolean caretVisible;

    public Searchbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

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

    public void addCharToSearch(char codePoint) {
        searchString.append(codePoint);
        caretPosition++;
        bookTab.scrollbar().resetScrollIndex();
    }

    public void removeCharFromSearch() {
        if(!searchString.isEmpty()) {
            searchString.deleteCharAt(caretPosition-1);
            caretPosition--;
            if(caretPosition < 0) caretPosition = 0;
            bookTab.scrollbar().resetScrollIndex();
        }
    }

    /**
     * Filters the book tab's enchantments based on search string.
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

    public void clearSearch() {
        searchString.setLength(0);
    }
}
