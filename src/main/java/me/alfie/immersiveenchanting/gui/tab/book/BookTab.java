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

    public BookTab(EnchantingTableScreen screen) {
        this.screen = screen;

        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.UNLOCKED));
        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.LOCKED));
        scrollbar = new Scrollbar(this);
        searchbar = new Searchbar(this);
    }

    public void init() {
        resetFilters();
        scrollbar().resetScrollIndex();
        searchbar.clearSearch();
    }

    public EnchantingTableScreen screen() {
        return screen;
    }

    public List<BookFilters> getEnabledFilters() {
        List<BookFilters> bookFilters = new ArrayList<>();
        for(FilterCheckbox checkbox : filterCheckboxes) {
            if(checkbox.isEnabled()) bookFilters.add(checkbox.filterType);
        }
        return bookFilters;
    }

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
                    CostRegistry.client().getAllEnchantmentHolders().stream()
                            .filter(e -> !unlocked.contains(e))
                            .toList()
            );
        }

        searchbar.checkSearch();
        renderedEnchantments.sort(Comparator.comparing(e -> e.value().description().getString()));
    }

    private void resetFilters() {
        for(FilterCheckbox filterCheckbox : filterCheckboxes) {
            filterCheckbox.setEnabled(true);
        }
    }

    public void render(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        renderFilterCheckboxes(graphics, mouseX, mouseY);
        renderEnchantmentBoxes(graphics);
        scrollbar.render(graphics, mouseX, mouseY);
        searchbar.render(graphics);
    }

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

    public List<FilterCheckbox> filterCheckboxes() {
        return filterCheckboxes;
    }

    public Scrollbar scrollbar() {
        return scrollbar;
    }

    public Searchbar searchbar() {
        return searchbar;
    }

    public List<Holder<Enchantment>> getRenderedEnchantments() {
        return renderedEnchantments;
    }

}
