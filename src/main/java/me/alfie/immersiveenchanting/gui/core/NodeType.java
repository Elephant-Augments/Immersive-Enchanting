package me.alfie.immersiveenchanting.gui.core;

import net.minecraft.resources.ResourceLocation;

public enum NodeType {
        BASIC(
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/basic_enchantment_unobtained.png"),
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/basic_enchantment_obtained.png")
        ),
        ADVANCED(
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/advanced_enchantment_unobtained.png"),
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/sprites/advanced_enchantment_obtained.png")
        ),
        ELITE(
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/elite_enchantment_unobtained.png"),
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/elite_enchantment_obtained.png")
        ),
        LOCKED(
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/basic_enchantment_locked.png"),
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/basic_enchantment_locked.png")
        ),
        ALERT(
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/warning_enchantment.png"),
                ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/sprites/warning_enchantment.png")
        );

    private final ResourceLocation unobtainedTexture;
    private final ResourceLocation obtainedTexture;

    NodeType(ResourceLocation offTexture, ResourceLocation onTexture) {
        this.unobtainedTexture = offTexture;
        this.obtainedTexture = onTexture;
    }

    public ResourceLocation getUnobtainedTexture() {
        return unobtainedTexture;
    }

    public ResourceLocation getObtainedTexture() {
        return obtainedTexture;
    }
}
