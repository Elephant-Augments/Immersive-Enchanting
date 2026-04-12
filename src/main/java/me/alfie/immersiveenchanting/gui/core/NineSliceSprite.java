package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.Identifier;

public enum NineSliceSprite {
    TOOLTIP_DESCRIPTION("tooltip/tooltip_description"),
    TOOLTIP_OBTAINED("tooltip/tooltip_obtained"),
    TOOLTIP_UNOBTAINED("tooltip/tooltip_unobtained");

    private final Identifier identifier;

    NineSliceSprite(String path) {
        this.identifier = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, path);
    }

    public Identifier id() {
        return identifier;
    }

}
