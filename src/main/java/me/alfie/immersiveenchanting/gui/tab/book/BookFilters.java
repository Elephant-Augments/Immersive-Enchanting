package me.alfie.immersiveenchanting.gui.tab.book;

import net.minecraft.network.chat.Component;

public enum BookFilters {
    UNLOCKED(Component.translatable("immersiveenchanting.filter.unlocked")),
    LOCKED(Component.translatable("immersiveenchanting.filter.locked"));

    private final Component label;

    BookFilters(Component label) {
        this.label = label;
    }

    public Component getLabel() {
        return label;
    }
}