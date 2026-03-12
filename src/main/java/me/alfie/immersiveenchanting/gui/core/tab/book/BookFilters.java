package me.alfie.immersiveenchanting.gui.core.tab.book;

import net.minecraft.network.chat.Component;

public enum BookFilters {
    UNLOCKED(Component.translatable("gui.immersiveenchanting.filter.unlocked")),
    LOCKED(Component.translatable("gui.immersiveenchanting.filter.locked"));

    private final Component label;

    // Constructor to initialize the texture and label for each filter
    BookFilters(Component label) {
        this.label = label;
    }

    // Getter method for the label
    public Component getLabel() {
        return label;
    }
}