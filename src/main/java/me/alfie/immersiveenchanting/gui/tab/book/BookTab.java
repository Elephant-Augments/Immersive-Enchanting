package me.alfie.immersiveenchanting.gui.tab.book;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BookTab {
    private final EnchantingTableScreen screen;

    private final List<Holder<Enchantment>> renderedEnchantments = new ArrayList<>();

    private final List<FilterCheckbox> filterCheckboxes = new ArrayList<>();
    private final Scrollbar scrollbar;
    private final Searchbar searchbar;


    public final int MAX_BOXES_RENDERED = 6;

    /**
     * Creates a new book tab instance for the enchanting screen.
     <P>
     * <p>Initializes filter checkboxes, scrollbar, and search bar components.</p>
     <P>
     * @param screen the parent {@link EnchantingTableScreen}
     */
    public BookTab(EnchantingTableScreen screen) {
        this.screen = screen;

        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.UNLOCKED));
        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.LOCKED));
        scrollbar = new Scrollbar(this);
        searchbar = new Searchbar(this);
    }

    /**
     * Initializes the book tab state.
     <P>
     * <p>Resets all filters, scroll position, and search input.</p>
     */
    public void init() {
        resetFilters();
        scrollbar().resetScrollIndex();
        searchbar.clearSearch();
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
     * Retrieves all currently enabled filters.
     <P>
     * @return a list of active {@link BookFilters}
     */
    public List<BookFilters> getEnabledFilters() {
        List<BookFilters> bookFilters = new ArrayList<>();
        for(FilterCheckbox checkbox : filterCheckboxes) {
            if(checkbox.isEnabled()) bookFilters.add(checkbox.filterType);
        }
        return bookFilters;
    }

    /**
     * Applies active filters and search criteria to determine which enchantments are rendered.
     <P>
     * <p>This method:</p>
     <P>
     * <ul>
     *     <li>Clears the current rendered enchantment list</li>
     *     <li>Adds unlocked enchantments if enabled</li>
     *     <li>Adds locked enchantments if enabled</li>
     *     <li>Applies search filtering</li>
     *     <li>Sorts the results alphabetically</li>
     * </ul>
     */
    private void applyFilters() {
        renderedEnchantments.clear();
        List<BookFilters> enabledFilters = getEnabledFilters();

        if(enabledFilters.contains(BookFilters.UNLOCKED)) {
            renderedEnchantments.addAll(screen.getMenu().getAvailableEnchantments());
        }

        if (enabledFilters.contains(BookFilters.LOCKED)) {
            Set<Holder<Enchantment>> unlocked =
                    new HashSet<>(screen.getMenu().getAvailableEnchantments());

            renderedEnchantments.addAll(
                    CostRegistry.client().getAllEnabledEnchantmentHolders().stream()
                            .filter(e -> !unlocked.contains(e))
                            .toList()
            );
        }

        searchbar.checkSearch();
        renderedEnchantments.sort(Comparator.comparing(e -> e.value().description().getString()));
    }

    /**
     * Resets all filters to their default enabled state.
     <P>
     * <p>All filter checkboxes are set to enabled.</p>
     */
    private void resetFilters() {
        for(FilterCheckbox filterCheckbox : filterCheckboxes) {
            filterCheckbox.setEnabled(true);
        }
    }

    /**
     * Renders the entire book tab UI.
     <P>
     * <p>Includes filter checkboxes, enchantment list, scrollbar, and search bar.</p>
     <P>
     * @param graphics the GUI rendering context
     * @param mouseX the current mouse X position
     * @param mouseY the current mouse Y position
     */
    public void render(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        renderFilterCheckboxes(graphics, mouseX, mouseY);
        renderEnchantmentBoxes(graphics);
        scrollbar.render(graphics, mouseX, mouseY);
        searchbar.render(graphics);
    }

    /**
     * Renders the list of enchantment boxes.
     <P>
     * <p>Displays up to {@code MAX_BOXES_RENDERED} entries based on the current scroll position.</p>
     <P>
     * <p>Empty boxes are rendered when there are fewer enchantments than available slots.</p>
     <P>
     * @param graphics the GUI rendering context
     */
    private void renderEnchantmentBoxes(GuiGraphicsExtractor graphics) {
        int x = screen.getGuiLeft() + 13;
        int y = screen.getGuiTop() + 6;

        for (int i = 0; i < MAX_BOXES_RENDERED; i++) {
            applyFilters();

            if (scrollbar.scrollIndex+i < renderedEnchantments.size()) {
                Holder<Enchantment> enchantmentHolder = renderedEnchantments.get(scrollbar.scrollIndex + i);
                EnchantmentBox box = new EnchantmentBox(this, enchantmentHolder);
                box.render(graphics, x, y);
            } else {
                EnchantmentBox box = new EnchantmentBox(this, null);
                box.render(graphics, x, y);
            }

            y += Sprite.ENCHANTMENT_BOX_LOCKED.height();
        }
    }

    /**
     * Renders filter checkboxes and the total enchantment count label.
     <P>
     * <p>Checkbox positions are dynamically calculated based on index.</p>
     <P>
     * @param graphics the GUI rendering context
     * @param mouseX the current mouse X position
     * @param mouseY the current mouse Y position
     */
    private void renderFilterCheckboxes(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int count = 0;
        final int spacing = 18;

        for(FilterCheckbox filterCheckbox : filterCheckboxes()) {
            filterCheckbox.setX(138);
            filterCheckbox.setY(12 + (count*spacing));
            filterCheckbox.render(graphics, mouseX, mouseY);
            count++;
        }

        final int xPos = screen.getGuiLeft() + 138;
        final int yPos = screen.getGuiTop() + (count * spacing) + 16;
        graphics.text(Minecraft.getInstance().font,
                Component.translatable("immersiveenchanting.label.total_enchantments", renderedEnchantments.size()),
                xPos, yPos, Color.WHITE.getRGB());
    }

    /**
     * Gets the list of filter checkbox components.
     <P>
     * @return the list of {@link FilterCheckbox} instances
     */
    public List<FilterCheckbox> filterCheckboxes() {
        return filterCheckboxes;
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
     * @return the {@link Searchbar}
     */
    public Searchbar searchbar() {
        return searchbar;
    }

    /**
     * Gets the list of enchantments currently being rendered.
     <P>
     * @return the filtered and sorted list of {@link Enchantment} holders
     */
    public List<Holder<Enchantment>> getRenderedEnchantments() {
        return renderedEnchantments;
    }

}
