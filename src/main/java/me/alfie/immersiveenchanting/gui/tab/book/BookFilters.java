package me.alfie.immersiveenchanting.gui.tab.book;

import net.minecraft.network.chat.Component;

/**
 * Enum representing filter categories used in the book tab UI.
 <P>
 * <p>Each filter corresponds to a specific subset of enchantments
 * based on their availability state.</p>
 <P>
 * <ul>
 *     <li>UNLOCKED → enchantments currently available to the player</li>
 *     <li>LOCKED → enchantments not currently available</li>
 * </ul>
 */
public enum BookFilters {
    UNLOCKED(Component.translatable("immersiveenchanting.filter.unlocked")),
    LOCKED(Component.translatable("immersiveenchanting.filter.locked"));

    private final Component label;

    BookFilters(Component label) {
        this.label = label;
    }

    /**
     * Retrieves the display label for this filter.
     <P>
     * @return the localized {@link Component} representing this filter
     */
    public Component getLabel() {
        return label;
    }
}