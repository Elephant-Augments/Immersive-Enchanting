package me.alfie.immersiveenchanting.gui.transmute;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeBranch;
import me.alfie.immersiveenchanting.gui.core.NodeType;
import net.minecraft.resources.ResourceLocation;

public class TransmuteNodeBranch extends NodeBranch {

    public TransmuteNodeBranch(EnchantingTableScreen screen, float branchAngle,
                               boolean canTransmute,
                               boolean isBookReplicated) {
        super(screen, branchAngle);

        NodeType nodeType = NodeType.ADVANCED;
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/item/ancient_book.png");

        boolean nodeUnlocked = true;
        if(!canTransmute || isBookReplicated) {
            nodeUnlocked = false;
        }

        TransmuteNode node = new TransmuteNode(
                nodeType,
                iconTexture,
                nodeUnlocked,
                isBookReplicated);

        addNode(node);
    }
}
