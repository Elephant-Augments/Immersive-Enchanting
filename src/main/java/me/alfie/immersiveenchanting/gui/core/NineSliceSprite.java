package me.alfie.immersiveenchanting.gui.core;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;

public enum NineSliceSprite {
    TOOLTIP_DESCRIPTION("tooltip/tooltip_description"),
    TOOLTIP_OBTAINED("tooltip/tooltip_obtained"),
    TOOLTIP_UNOBTAINED("tooltip/tooltip_unobtained");

    private final ResourceId id;

    NineSliceSprite(String path) {
        this.id = new ResourceId(ImmersiveEnchanting.MODID, path);
    }

    public ResourceId id() {
        return id;
    }

}
