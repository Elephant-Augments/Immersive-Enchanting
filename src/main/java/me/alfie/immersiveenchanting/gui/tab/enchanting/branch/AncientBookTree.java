package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

import me.alfie.immersiveenchanting.api.node.*;
import me.alfie.immersiveenchanting.api.node.internal.ReplicateNodeData;
import me.alfie.immersiveenchanting.api.node.internal.TransmuteNodeData;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import me.alfie.immersiveenchanting.util.EnchantmentTextureHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.network.chat.Component;

/**
 * Builds the ancient book transmute/replicate branches.
 */
public final class AncientBookTree {

    private AncientBookTree() {}

    public static void addAncientBookBranches(BuildBranchesEvent event) {
        addTransmuteBranch(event);
        addReplicateBranch(event);
    }

    private static void addTransmuteBranch(BuildBranchesEvent event) {
        NodeState state = event.getCanvas().screen().getMenu().isEnchantmentAvailable(
                EnchantmentUtil.getStoredEnchantment(event.getStack()))
                ? NodeState.UNOBTAINED : NodeState.LOCKED;
        if(EnchantmentUtil.isReplicated(event.getStack())) state = NodeState.ALERT;

        NodeTemplate transmuteNode = new NodeTemplate(
                Component.translatable("immersiveenchanting.tooltip.title.transmute"),
                0,
                state,
                NodeTier.ADVANCED,
                new SpriteIcon(EnchantmentTextureHelper.getTexture(CostRegistry.TRANSMUTE)),
                TransmuteNodeData.create()
        );

        if(!event.costRegistry().isRegistered(CostRegistry.TRANSMUTE)
                || event.costRegistry().get(CostRegistry.TRANSMUTE).enabled()) {
            event.addBranch(BranchBuilder.of(event.getCanvas(), CostRegistry.TRANSMUTE)
                    .node(transmuteNode)
                    .build());
        }
    }

    private static void addReplicateBranch(BuildBranchesEvent event) {
        NodeTemplate replicateNode = new NodeTemplate(
                Component.translatable("immersiveenchanting.tooltip.title.replicate"),
                0,
                NodeState.UNOBTAINED,
                NodeTier.ADVANCED,
                new SpriteIcon(EnchantmentTextureHelper.getTexture(CostRegistry.REPLICATE)),
                ReplicateNodeData.create()
        );

        if(!event.costRegistry().isRegistered(CostRegistry.REPLICATE)
                || event.costRegistry().get(CostRegistry.REPLICATE).enabled()) {
            event.addBranch(BranchBuilder.of(event.getCanvas(), CostRegistry.REPLICATE)
                    .node(replicateNode)
                    .build());
        }
    }
}
