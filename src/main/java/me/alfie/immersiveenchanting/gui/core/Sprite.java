package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.ResourceLocation;

public enum Sprite {
    ENCHANTING_TABLE_BACKGROUND("textures/gui/container/enchanting_table.png"),
    ENCHANTING_TILE("textures/gui/container/enchanting_background_tile.png"),
    BOOKS_TILE("textures/gui/container/books_background_tile.png"),
    BOOK_OPEN("textures/gui/container/book_open_shadow.png"),
    ENCHANTING_TABLE_TOP("textures/gui/container/enchanting_table_top.png"),
    BOOK_CLOSED("textures/gui/container/book_closed.png"),
    LEVEL("textures/gui/sprites/level_10.png"),
    XP_LEVEL("textures/gui/sprites/xp_level.png"),

    SCROLLBAR("textures/gui/book_window/scrollbar.png"),
    SCROLLER("textures/gui/book_window/scroller.png"),
    SEARCH("textures/gui/book_window/search.png"),
    CHECKBOX_ON("textures/gui/book_window/checkbox_on.png"),
    CHECKBOX_OFF("textures/gui/book_window/checkbox_off.png"),
    ENCHANTMENT_BOX_UNLOCKED("textures/gui/sprites/enchantment_box_unlocked.png"),
    ENCHANTMENT_BOX_LOCKED("textures/gui/sprites/enchantment_box_locked.png"),
    BACKGROUND_BOX("textures/gui/book_window/background_box.png");

    private final ResourceLocation location;

    Sprite(String path) {
        this.location = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, path);
    }

    public ResourceLocation get() {
        return location;
    }
}