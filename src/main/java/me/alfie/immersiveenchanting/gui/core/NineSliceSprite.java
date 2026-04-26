package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.ResourceLocation;

public enum NineSliceSprite {
    TOOLTIP_DESCRIPTION("tooltip/tooltip_description"),
    TOOLTIP_OBTAINED("tooltip/tooltip_obtained"),
    TOOLTIP_UNOBTAINED("tooltip/tooltip_unobtained");

    private final ResourceLocation identifier;

    NineSliceSprite(String path) {
        this.identifier = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, path);
    }

    public ResourceLocation id() {
        return identifier;
    }

}
