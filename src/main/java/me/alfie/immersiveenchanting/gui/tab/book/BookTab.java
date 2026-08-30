package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableLayout;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.*;

/**
 * Represents the "Ancient Books" tab in the {@link EnchantingTableScreen}.
 *
 * <p>This tab is responsible for rendering and managing the book browsing
 * interface, including:
 * <ul>
 *     <li>The search bar</li>
 *     <li>The scrollable list of enchantment boxes</li>
 *     <li>The scrollbar</li>
 * </ul>
 *
 * <p>The visible list is filtered by the hold-to-filter picker (mod, all, or
 * unlocked) and by the current search string.</p>
 */
public class BookTab {
    private final EnchantingTableScreen screen;

    private final List<Holder<Enchantment>> renderedEnchantments = new ArrayList<>();

    private final Scrollbar scrollbar;
    private final BookSearchbar bookSearchbar;


    public final int MAX_BOXES_RENDERED = 7;

    /**
     * Constructs the book tab and initializes its core components.
     *
     * <p>Initializes:
     * <ul>
     *     <li>{@link Scrollbar} for navigating the enchantment list</li>
     *     <li>{@link BookSearchbar} for filtering by name/tooltip text</li>
     * </ul>
     *
     * @param screen The parent {@link EnchantingTableScreen}
     */
    public BookTab(EnchantingTableScreen screen) {
        this.screen = screen;
        scrollbar = new Scrollbar(this);
        bookSearchbar = new BookSearchbar(this);
    }

    /**
     * Initializes the book tab state.
     <P>
     * <p>Resets search input and scrollbar position when the tab is opened.</p>
     */
    public void init() {
        scrollbar().resetScrollIndex();
        bookSearchbar.clearSearch();
    }

    /**
     * Gets the parent enchanting table screen.
     <P>
     * @return the {@link EnchantingTableScreen} associated with this tab
     */
    public EnchantingTableScreen screen() {
        return screen;
    }

    /**
     * Applies active filters and search criteria to determine which enchantments are rendered.
     <P>
     * <p>This method:</p>
     <P>
     * <ul>
     *     <li>Clears the current rendered enchantment list</li>
     *     <li>Rebuilds the visible enchantment list from the current picker filter</li>
     *     <li>Applies search filtering</li>
     *     <li>Sorts the results alphabetically</li>
     * </ul>
     */
    private void applyFilters() {
        renderedEnchantments.clear();
        var source = screen.isShowingUnlockedOnly()
                ? screen.getMenu().getAvailableEnchantments().stream()
                : CostRegistry.client().getAllEnabledEnchantmentHolders().stream();
        renderedEnchantments.addAll(source.filter(screen::matchesCurrentFilter).toList());

        bookSearchbar.checkSearch();
        renderedEnchantments.sort(Comparator.comparing(e -> e.value().description().getString()));
    }

    /**
     * Renders the book tab UI.
     *
     * <p>This includes:
     * <ul>
     *     <li>The search bar</li>
     *     <li>The total-enchantment count</li>
     *     <li>The visible enchantment boxes</li>
     *     <li>The scrollbar</li>
     * </ul>
     */
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        applyFilters();
        bookSearchbar.render(gx);
        renderTotalLabel(gx);
        renderEnchantmentBoxes(gx);
        scrollbar.render(gx, mousePos);
    }

    /**
     * Draws the visible slice of enchantment boxes for the current scroll index.
     */
    private void renderEnchantmentBoxes(GuiGraphicsX gx) {
        int x = screen.getGuiLeft() + EnchantingTableLayout.BOOK_LIST_X;
        int y = screen.getGuiTop() + EnchantingTableLayout.BOOK_LIST_Y;

        for (int i = 0; i < MAX_BOXES_RENDERED; i++) {
            if (scrollbar.scrollIndex + i < renderedEnchantments.size()) {
                Holder<Enchantment> enchantmentHolder = renderedEnchantments.get(scrollbar.scrollIndex + i);
                EnchantmentBox box = new EnchantmentBox(this, enchantmentHolder);
                box.render(gx, x, y);
            } else {
                EnchantmentBox box = new EnchantmentBox(this, null);
                box.render(gx, x, y);
            }

            y += Sprite.ENCHANTMENT_BOX_LOCKED.height();
        }
    }

    /**
     * Draws the total number of enchantments matching the current filters.
     */
    private void renderTotalLabel(GuiGraphicsX gx) {
        int x = screen.getGuiLeft() + EnchantingTableLayout.BOOK_LIST_X;
        int y = screen.getGuiTop() + EnchantingTableLayout.BOOK_TOTAL_LABEL_Y;
        GuiGraphicsApi.text(
                gx, screen().getFont(),
                Component.translatable("immersiveenchanting.label.total_enchantments", renderedEnchantments.size()),
                x, y, true);
    }

    /**
     * @return the filtered and sorted list of {@link Enchantment} holders
     */
    public List<Holder<Enchantment>> getRenderedEnchantments() {
        applyFilters();
        return renderedEnchantments;
    }

    /**
     * Current list while filters are being applied. Does not rebuild the list.
     */
    List<Holder<Enchantment>> currentRenderedEnchantments() {
        return renderedEnchantments;
    }

    /**
     * Gets the scrollbar component for this tab.
     <P>
     * @return the {@link Scrollbar}
     */
    public Scrollbar scrollbar() {
        return scrollbar;
    }

    /**
     * Gets the search bar component.
     <P>
     * @return the {@link BookSearchbar}
     */
    public BookSearchbar bookSearchbar() {
        return bookSearchbar;
    }

}
