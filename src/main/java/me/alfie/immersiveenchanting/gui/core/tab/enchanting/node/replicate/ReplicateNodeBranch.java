package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.replicate;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeBranch;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeType;
import net.minecraft.resources.ResourceLocation;

public class ReplicateNodeBranch extends NodeBranch {

    public ReplicateNodeBranch(EnchantingTableScreen screen, float branchAngle) {
        super(screen, branchAngle);

        NodeType nodeType = NodeType.ADVANCED;
        ResourceLocation iconTexture = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "textures/item/ancient_book.png");

        ReplicateNode node = new ReplicateNode(
                nodeType,
                iconTexture);

        addNode(node);
    }
}
