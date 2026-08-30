package me.alfie.immersiveenchanting.gui.tab.enchanting;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.EnchantingTableLayout;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import net.minecraft.network.chat.Component;

/**
 * Search bar for the enchanting tree viewport.
 *
 * <p>Activates universal {@link
 * me.alfie.immersiveenchanting.api.filter.EnchantmentFilterSelection.Search} mode on the
 * first keystroke, bypassing the active hold-to-filter selection.</p>
 */
public class EnchantingSearchbar {

    private final EnchantingTab enchantingTab;

    private final StringBuilder searchString = new StringBuilder();
    private int caretPosition = 0;
    private long lastCaretTime = 0;
    private boolean caretVisible;
    private boolean focused;

    public EnchantingSearchbar(EnchantingTab enchantingTab) {
        this.enchantingTab = enchantingTab;
    }

    public boolean isVisible() {
        return enchantingTab.screen().isState(ScreenState.ENCHANTING)
                && enchantingTab.isDisplay(EnchantingTab.Display.ENCHANTMENTS);
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
                EnchantingTableLayout.ENCHANTING_SEARCH_WIDTH
        );
    }

    public boolean onMouseClick(MousePos mousePos, int button) {
        if(!isVisible() || button != 0) {
            return false;
        }

        if(contains(mousePos)) {
            setFocused(true);
            return true;
        }

        return false;
    }

    public void render(GuiGraphicsX gx) {
        if(!isVisible()) {
            return;
        }

        int xPos = screenX();
        int yPos = screenY();

        EnchantingTableLayout.renderSearchField(
                gx,
                enchantingTab.screen().getFont(),
                xPos,
                yPos,
                searchString.toString(),
                EnchantingTableLayout.ENCHANTING_SEARCH_WIDTH
        );

        if(focused) {
            renderCaret(gx, xPos + EnchantingTableLayout.SEARCH_FIELD_TEXT_PADDING, yPos + EnchantingTableLayout.SEARCH_FIELD_TEXT_PADDING + 1);
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
            int textWidth = enchantingTab.screen().getFont().width(searchString.toString());
            GuiGraphicsApi.text(gx, enchantingTab.screen().getFont(), Component.literal("_"),
                    x + textWidth, y, true);
        }
    }

    public void addCharToSearch(char codePoint) {
        if(!isVisible() || !focused || !Character.isDefined(codePoint) || Character.isISOControl(codePoint)) {
            return;
        }

        enchantingTab.screen().enterSearchFilterMode();
        searchString.insert(caretPosition, codePoint);
        caretPosition++;
        onSearchChanged();
    }

    public void removeCharFromSearch() {
        if(!isVisible() || !focused || searchString.isEmpty() || caretPosition <= 0) {
            return;
        }

        searchString.deleteCharAt(caretPosition - 1);
        caretPosition--;
        onSearchChanged();
    }

    public String query() {
        return searchString.toString();
    }

    /** Clears the typed query without restoring picker filter state. */
    public void clearInput() {
        searchString.setLength(0);
        caretPosition = 0;
        setFocused(false);
    }

    private void onSearchChanged() {
        if(searchString.isEmpty()) {
            enchantingTab.screen().exitSearchFilterMode();
        } else {
            enchantingTab.screen().rebuildBranches();
        }
    }

    private int screenX() {
        return enchantingTab.screen().getGuiLeft() + EnchantingTableLayout.ENCHANTING_SEARCH_X;
    }

    private int screenY() {
        return enchantingTab.screen().getGuiTop() + EnchantingTableLayout.ENCHANTING_SEARCH_Y;
    }
}
