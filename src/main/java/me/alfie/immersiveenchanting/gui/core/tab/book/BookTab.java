package me.alfie.immersiveenchanting.gui.core.tab.book;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.lootmodifier.AncientBookLootModifier;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookTab {
    public final EnchantingTableScreen screen;

    public final List<FilterCheckbox> filterCheckboxes = new ArrayList<>();
    public final List<EnchantmentBox> enchantmentBoxes = new ArrayList<>();
    public final Scrollbar scrollbar;
    public final Searchbar searchbar;

    private final List<Holder<Enchantment>> enchantments = new ArrayList<>(); //Enchantments to display

    public final int MAX_BOXES_RENDERED = 6;

    public BookTab(EnchantingTableScreen enchantingTableScreen) {
        screen = enchantingTableScreen;

        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.UNLOCKED));
        filterCheckboxes.add(new FilterCheckbox(this, BookFilters.LOCKED));
        scrollbar = new Scrollbar(this);
        searchbar = new Searchbar(this);
    }

    public void render(GuiGraphics guiGraphics) {
        guiGraphics.setColor(0.5F, 0.5F, 0.5F, 1f);
        screen.getCanvas().renderTiledBg(guiGraphics);

        renderFilters(guiGraphics);
        renderEnchantmentBoxes(guiGraphics);
        scrollbar.render(guiGraphics);
        searchbar.render(guiGraphics);
    }

    private void renderFilters(GuiGraphics guiGraphics) {
        int count = 0;
        final int spacing = 18;
        for(FilterCheckbox filterCheckbox : filterCheckboxes) {
            filterCheckbox.setX(138);
            filterCheckbox.setY(12 + (count*spacing));
            filterCheckbox.render(guiGraphics);
            count++;
        }

        //Show total
        guiGraphics.drawString(screen.getMinecraft().font,
                Component.translatable("gui.immersiveenchanting.total_enchantments", enchantments.size()),
                screen.getGuiLeft() + 138, screen.getGuiTop() + 12 + (count*spacing) + 4,
                Color.WHITE.hashCode());
    }

    private void renderEnchantmentBoxes(GuiGraphics guiGraphics) {
        enchantmentBoxes.clear();
        int x = screen.getGuiLeft() + 13;
        int y = screen.getGuiTop() + 6;

        for (int i = 0; i < 6; i++) { //6 boxes on screen
            final int spriteHeight = 19;
            applyFilters();

            //Render enchantments
            if (scrollbar.scrollIndex+i < enchantments.size()) {
                Holder<Enchantment> enchantmentHolder = enchantments.get(scrollbar.scrollIndex + i);
                EnchantmentBox box = new EnchantmentBox(this, enchantmentHolder);
                enchantmentBoxes.add(box);
                box.render(guiGraphics, x, y);

                //Blank box if list too short.
            } else {
                EnchantmentBox box = new EnchantmentBox(this, null);
                enchantmentBoxes.add(box);
                box.render(guiGraphics, x, y);
            }

            // Update the vertical position
            y += spriteHeight;
        }
    }

    public List<Holder<Enchantment>> getEnchantments() {
        return enchantments;
    }

    private void applyFilters() {
        enchantments.clear();
        List<BookFilters> enabledFilters = getEnabledFilters();

        if(enabledFilters.contains(BookFilters.UNLOCKED)) {
            enchantments.addAll(screen.getMenu().getUnlockedEnchantments());
        }

        if(enabledFilters.contains(BookFilters.LOCKED)) {
            List<Holder.Reference<Enchantment>> allEnchantments = EnchantmentUtil.getAllEnchantments(screen.player.level());
            List<Holder<Enchantment>> unlockedEnchantments = screen.getMenu().getUnlockedEnchantments().stream().toList();

            for(Holder.Reference<Enchantment> enchantmentReference : allEnchantments) {
                if(!unlockedEnchantments.contains(enchantmentReference.getDelegate())) {
                    enchantments.add(enchantmentReference); //Add the locked enchantment
                }
            }
        }

        searchbar.checkSearch();

        //Always sort final list alphabetically
        enchantments.sort(Comparator.comparing(e -> e.value().description().getString()));
    }

    public void resetFilterBoxes() {
        for(FilterCheckbox filterCheckbox : filterCheckboxes) {
            filterCheckbox.setEnabled(true);
        }
    }

    public List<BookFilters> getEnabledFilters() {
        List<BookFilters> bookFilters = new ArrayList<>();
        for(FilterCheckbox checkbox : filterCheckboxes) {
            if(checkbox.isEnabled()) bookFilters.add(checkbox.filterType);
        }
        return bookFilters;
    }
}
