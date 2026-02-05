package me.alfie.immersiveenchanting.gui;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantingNode extends Node {

    public static float globalScale = 1.0f;
    public final boolean isBranchUnlocked;


    private final int enchantmentLevel;
    private final ResourceKey<Enchantment> enchantment;
    private final Holder<Enchantment> enchantmentHolder;
    private final ResourceLocation LOCKED_ICON = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/gui/enchantment_icons/locked_enchantment.png");

    public EnchantingNode(NodeType nodeType, ResourceLocation iconTexture,
                          int enchantmentLevel,
                          Holder<Enchantment> enchantmentHolder,
                          boolean isBranchUnlocked) {
        super(nodeType, iconTexture);

        this.enchantmentLevel = enchantmentLevel;
        this.enchantmentHolder = enchantmentHolder;
        this.isBranchUnlocked = isBranchUnlocked;

        this.enchantment = enchantmentHolder.getKey();

        if (!isBranchUnlocked) {
            setNodeType(NodeType.LOCKED);
            setIconTexture(null);
        }
    }


    public int getEnchantmentLevel() {
        return enchantmentLevel;
    }

    public Holder<Enchantment> getEnchantmentHolder() {
        return enchantmentHolder;
    }

    public ResourceKey<Enchantment> getEnchantment() {
        return enchantment;
    }

}
