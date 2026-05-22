package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.ResourceLocation;

public enum NineSliceSprite {
    TOOLTIP_DESCRIPTION("tooltip/tooltip_description"),
    TOOLTIP_OBTAINED("tooltip/tooltip_obtained"),
    TOOLTIP_UNOBTAINED("tooltip/tooltip_unobtained");

    private final ResourceLocation id;

    NineSliceSprite(String path) {
        this.id = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, path);
    }

    public ResourceLocation id() {
        return id;
    }

}
