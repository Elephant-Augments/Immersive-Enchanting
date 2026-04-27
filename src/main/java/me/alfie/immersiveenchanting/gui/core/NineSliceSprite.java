package me.alfie.immersiveenchanting.gui.core;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.ResourceLocation;

public enum NineSliceSprite {
    TOOLTIP_WIDGETS("textures/gui/sprites/tooltip/tooltip_widgets.png");


    private final ResourceLocation identifier;

    NineSliceSprite(String path) {
        this.identifier = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, path);
    }

    public ResourceLocation id() {
        return identifier;
    }

}
