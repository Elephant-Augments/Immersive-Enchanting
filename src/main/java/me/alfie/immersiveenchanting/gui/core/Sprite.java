package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.ResourceLocation;

public enum Sprite {
    ENCHANTING_TABLE_BACKGROUND("textures/gui/container/enchanting_table.png"),
    TILE("textures/gui/container/background.png"),
    BOOK_OPEN("textures/gui/container/book_open_shadow.png"),
    ENCHANTING_TABLE_TOP("textures/gui/container/enchanting_table_top.png"),
    BOOK_CLOSED("textures/gui/container/book_closed.png"),
    LEVEL("textures/gui/sprites/level_10.png"),
    XP_LEVEL("textures/gui/sprites/xp_level.png");

    private final ResourceLocation location;

    Sprite(String path) {
        this.location = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, path);
    }

    public ResourceLocation get() {
        return location;
    }
}