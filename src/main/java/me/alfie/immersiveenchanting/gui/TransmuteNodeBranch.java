package me.alfie.immersiveenchanting.gui;

import net.minecraft.resources.ResourceLocation;

public class TransmuteNodeBranch extends NodeBranch {

    public TransmuteNodeBranch(EnchantingTableScreen screen, float branchAngle, boolean canTransmute) {
        super(screen, branchAngle);

        NodeType nodeType = NodeType.ADVANCED;
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/item/ancient_book.png");

        TransmuteNode node = new TransmuteNode(
                nodeType,
                iconTexture,
                canTransmute);

        addNode(node);
    }
}
