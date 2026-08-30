package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.EnchantingTableLayout;
import me.alfie.immersiveenchanting.util.EnchantmentSearchHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

/**
 * A text input component used in the {@link BookTab} to filter enchantments.
 *
 * <p>This search bar allows players to dynamically filter the visible
 * enchantments list by typing part of an enchantment's name or tooltip text.</p>
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
public class BookSearchbar {

    private final BookTab bookTab;

    private final StringBuilder searchString = new StringBuilder();
    private int caretPosition = 0;
    private long lastCaretTime = 0;
    private boolean caretVisible;
    private boolean focused;

    public BookSearchbar(BookTab bookTab) {
        this.bookTab = bookTab;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean contains(MousePos mousePos) {
        return EnchantingTableLayout.searchFieldContains(
                mousePos.x(),
                mousePos.y(),
                screenX(),
                screenY(),
                EnchantingTableLayout.BOOK_SEARCH_WIDTH
        );
    }

    public boolean onMouseClick(MousePos mousePos, int button) {
        if(button != 0) {
            return false;
        }

        if(contains(mousePos)) {
            setFocused(true);
            return true;
        }

        return false;
    }

    public void render(GuiGraphicsX gx) {
        int xPos = screenX();
        int yPos = screenY();

        EnchantingTableLayout.renderSearchField(
                gx,
                bookTab.screen().getFont(),
                xPos,
                yPos,
                searchString.toString(),
                EnchantingTableLayout.BOOK_SEARCH_WIDTH
        );

        if(focused) {
            renderCaret(
                    gx,
                    xPos + EnchantingTableLayout.SEARCH_FIELD_TEXT_PADDING,
                    yPos + EnchantingTableLayout.SEARCH_FIELD_TEXT_PADDING + 1
            );
        }
    }

    private void renderCaret(GuiGraphicsX gx, int x, int y) {
        long currentTime = System.currentTimeMillis();
        final int caretSpeed = 500;
        if(currentTime - lastCaretTime > caretSpeed) {
            caretVisible = !caretVisible;
            lastCaretTime = currentTime;
        }

        if(caretVisible) {
            int textWidth = bookTab.screen().getFont().width(searchString.toString());
            GuiGraphicsApi.text(gx, bookTab.screen().getFont(), Component.literal("_"),
                    x + textWidth, y, true);
        }
    }

    public void addCharToSearch(char codePoint) {
        if(!focused || !Character.isDefined(codePoint) || Character.isISOControl(codePoint)) {
            return;
        }

        searchString.insert(caretPosition, codePoint);
        caretPosition++;
        bookTab.scrollbar().resetScrollIndex();
    }

    public void removeCharFromSearch() {
        if(!focused || searchString.isEmpty() || caretPosition <= 0) {
            return;
        }

        searchString.deleteCharAt(caretPosition - 1);
        caretPosition--;
        bookTab.scrollbar().resetScrollIndex();
    }

    protected void checkSearch() {
        List<Holder<Enchantment>> rendered = bookTab.currentRenderedEnchantments();
        List<Holder<Enchantment>> toRemove = new ArrayList<>();

        for(Holder<Enchantment> enchantmentHolder : rendered) {
            if(!EnchantmentSearchHelper.matches(enchantmentHolder, searchString.toString())) {
                toRemove.add(enchantmentHolder);
            }
        }

        rendered.removeAll(toRemove);
    }

    public void clearSearch() {
        searchString.setLength(0);
        caretPosition = 0;
        setFocused(false);
    }

    private int screenX() {
        return bookTab.screen().getGuiLeft() + EnchantingTableLayout.BOOK_LIST_X;
    }

    private int screenY() {
        return bookTab.screen().getGuiTop() + EnchantingTableLayout.BOOK_SEARCH_Y;
    }
}
