package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.lootmodifier.AncientBookLootModifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AncientBookWindow {
    private final EnchantingTableScreen screen;

    private int scrollIndex = 0;
    public final List<FilterCheckbox> filterCheckboxes = new ArrayList<>();
    private final List<Holder<Enchantment>> enchantments = new ArrayList<>(); //Enchantments to display

    private int caretPosition = 0;
    private final StringBuilder searchString = new StringBuilder();

    private final int MAX_BOXES_RENDERED = 6;

    public AncientBookWindow(EnchantingTableScreen enchantingTableScreen) {
        screen = enchantingTableScreen;

        filterCheckboxes.add(new FilterCheckbox(screen, BookFilters.UNLOCKED));
        filterCheckboxes.add(new FilterCheckbox(screen, BookFilters.LOCKED));
    }

    public List<BookFilters> getEnabledFilters() {
        List<BookFilters> bookFilters = new ArrayList<>();
        for(FilterCheckbox checkbox : filterCheckboxes) {
            if(checkbox.isEnabled()) bookFilters.add(checkbox.filterType);
        }
        return bookFilters;
    }

    public void renderFilters(GuiGraphics guiGraphics) {
        int count = 0;
        final int spacing = 18;
        for(FilterCheckbox filterCheckbox : filterCheckboxes) {
            filterCheckbox.setX(138);
            filterCheckbox.setY(12 + (count*spacing));
            filterCheckbox.render(guiGraphics);
            count++;
        }
    }

    public void renderScrollbar(GuiGraphics guiGraphics) {
        int x = screen.getGuiLeft() + 121;
        int y = screen.getGuiTop() + 6;

        int scrollbarHeight = 114;

        guiGraphics.blit(
                Sprite.SCROLLBAR.get(),
                x,
                y,
                0f, 0f, 14, scrollbarHeight,
                14, scrollbarHeight
        );

        final int scrollerHeight = 15;

        int bottom = scrollbarHeight-scrollerHeight-2;

        int totalEnchantments = enchantments.size() + 1;
        double segmentHeight = (double) scrollbarHeight / totalEnchantments;
        double yOffset = scrollIndex * segmentHeight;

        // If we're at the last scroll index, force the scroller to the bottom of the scrollbar
        if (scrollIndex == enchantments.size() - MAX_BOXES_RENDERED) {
            yOffset = bottom;
        }
        int roundedYOffset = (int) Math.round(yOffset);

        guiGraphics.blit(
                Sprite.SCROLLER.get(),
                x+1,
                y+1+roundedYOffset,
                0f, 0f, 12, scrollerHeight,
                12, scrollerHeight
        );
    }

    public void renderSearch(GuiGraphics guiGraphics) {
        int x = screen.getGuiLeft() + 138;
        int y = screen.getGuiTop() + 100;

        guiGraphics.blit(
                Sprite.SEARCH.get(),
                x,
                y,
                0f, 0f, 100, 20,
                100, 20
        );

        final int textPadding = 4;
        guiGraphics.drawString(screen.getMinecraft().font,
                searchString.toString(),
                x+textPadding, y+textPadding+2, Color.WHITE.hashCode());
    }

    public void renderEnchantmentBoxes(GuiGraphics guiGraphics) {
        int x = screen.getGuiLeft() + 13;
        int y = screen.getGuiTop() + 6;

        guiGraphics.blit(
                Sprite.BACKGROUND_BOX.get(),
                x,
                y,
                0f, 0f, 108, 114,
                108, 114
        );

        for (int i = 0; i < 6; i++) { //6 boxes on screen
            final int spriteHeight = 19;
            applyFilters();

            //Render enchantments
            if (scrollIndex+i < enchantments.size()) {
                Holder<Enchantment> enchantmentHolder = enchantments.get(scrollIndex + i);

                // Determine which sprite to use based on whether the enchantment is unlocked
                Sprite boxSprite = screen.getMenu().isEnchantmentUnlocked(enchantmentHolder) ?
                        Sprite.ENCHANTMENT_BOX_UNLOCKED : Sprite.ENCHANTMENT_BOX_LOCKED;

                // Render the sprite
                guiGraphics.blit(
                        boxSprite.get(),
                        x,
                        y,
                        0f, 0f, 108, spriteHeight,
                        108, spriteHeight
                );

                // Render the enchantment title
                renderEnchantmentTitle(guiGraphics, enchantmentHolder, x, y);

            //Blank box if list too short.
            } else {
                guiGraphics.blit(
                        Sprite.ENCHANTMENT_BOX_LOCKED.get(),
                        x,
                        y,
                        0f, 0f, 108, spriteHeight,
                        108, spriteHeight
                );
            }

            // Update the vertical position
            y += spriteHeight;
        }
    }

    private void applyFilters() {
        enchantments.clear();
        List<BookFilters> enabledFilters = getEnabledFilters();

        if(enabledFilters.contains(BookFilters.UNLOCKED)) {
            enchantments.addAll(screen.getMenu().getUnlockedEnchantments());
        }

        if(enabledFilters.contains(BookFilters.LOCKED)) {
            List<Holder.Reference<Enchantment>> allEnchantments = AncientBookLootModifier.getAllEnchantments(screen.player.level());
            List<Holder<Enchantment>> unlockedEnchantments = screen.getMenu().getUnlockedEnchantments().stream().toList();

            for(Holder.Reference<Enchantment> enchantmentReference : allEnchantments) {
                if(!unlockedEnchantments.contains(enchantmentReference.getDelegate())) {
                    enchantments.add(enchantmentReference); //Add the locked enchantment
                }
            }
        }

        checkSearch();

        //Always sort final list alphabetically
        enchantments.sort(Comparator.comparing(e -> e.value().description().getString()));
    }

    private void renderEnchantmentTitle(GuiGraphics guiGraphics, Holder<Enchantment> enchantmentHolder, int x, int y) {
        final int padding = 4;
        guiGraphics.pose().pushPose();

        float scale = 1.0f;
        Component enchantmentTitle = enchantmentHolder.value().description();
        if(enchantmentTitle.getString().length() > 15) {
            scale = 0.75f;
            guiGraphics.pose().scale(scale, scale, scale);
        }

        int scaledX = (int)((x + padding) / scale);
        int scaledY = (int)((y + padding) / scale);

        int color = screen.getMenu().isEnchantmentUnlocked(enchantmentHolder) ?
                Color.WHITE.hashCode() : Color.GRAY.hashCode();

        guiGraphics.drawString(screen.getMinecraft().font, enchantmentTitle, scaledX, scaledY,
                color);
        guiGraphics.pose().popPose();
    }

    public void incrementScrollIndex(int increment) {
        scrollIndex += increment;
        if(scrollIndex > enchantments.size()-MAX_BOXES_RENDERED) scrollIndex = enchantments.size()-MAX_BOXES_RENDERED;
        if(scrollIndex < 0) scrollIndex = 0;
    }

    public void resetScrollIndex() {
        scrollIndex = 0;
    }

    public void addCharToSearch(char codePoint) {
        searchString.append(codePoint);
        caretPosition++;
        resetScrollIndex();
    }

    public void removeCharFromSearch() {
        if(!searchString.isEmpty()) {
            searchString.deleteCharAt(caretPosition-1);
            caretPosition--;
            if(caretPosition < 0) caretPosition = 0;
            resetScrollIndex();
        }
    }

    private void checkSearch() {
        List<Holder<Enchantment>> toRemove = new ArrayList<>();

        for (Holder<Enchantment> enchantmentHolder : enchantments) {
            String enchantmentName = enchantmentHolder.value().description().getString();

            // If the enchantment doesn't match the search query, add it to the remove list
            if (!enchantmentName.toLowerCase().contains(searchString.toString().toLowerCase())) {
                toRemove.add(enchantmentHolder);
            }
        }

        // Remove the collected enchantments after the loop finishes
        enchantments.removeAll(toRemove);
    }

    public void clearSearch() {
        searchString.setLength(0);
    }

    public void resetFilters() {
        for(FilterCheckbox filterCheckbox : filterCheckboxes) {
            filterCheckbox.setEnabled(true);
        }
    }
}
