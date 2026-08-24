package me.alfie.immersiveenchanting.api.filter.internal;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.node.BranchBuilder;
import me.alfie.immersiveenchanting.api.node.BuildBranchesEvent;
import me.alfie.immersiveenchanting.api.node.ItemIcon;
import me.alfie.immersiveenchanting.api.node.NodeData;
import me.alfie.immersiveenchanting.api.node.NodeTemplate;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeTier;
import net.minecraft.network.chat.Component;

/** Shared helpers for built-in {@link me.alfie.immersiveenchanting.api.filter.GlobalFilterBranch} implementations. */
final class GlobalFilterBranchSupport {

    private GlobalFilterBranchSupport() {}

    static ResourceId createPickerBranchId(String filterId) {
        final String modFilterStem = "mod_filter/";
        return new ResourceId(ImmersiveEnchanting.MODID, modFilterStem + filterId);
    }

    static void buildFilterBranch(
            BuildBranchesEvent event,
            ResourceId branchId,
            Component title,
            NodeTier tier,
            ItemIcon itemIcon,
            boolean selected,
            NodeData<?> nodeData
    ) {
        NodeState state = selected ? NodeState.OBTAINED : NodeState.UNOBTAINED;
        event.addBranch(BranchBuilder.of(event.getCanvas(), branchId)
                .node(new NodeTemplate(
                        title,
                        0,
                        state,
                        tier,
                        itemIcon,
                        nodeData
                )).build());
    }
}
