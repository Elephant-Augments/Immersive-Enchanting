package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.description.internal.lines.ModFilterLine;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.BranchFactory;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeType;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ModFilterLayoutExtension implements DescriptionLayoutExtension {
    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().isType(NodeType.MOD_FILTER)) return;
        tooltip.setLockingAllowed(false);


        Component title = tooltip.node().getTitle();
        Component component = Component.empty();
        if(tooltip.node().isState(NodeState.OBTAINED)) {
            component = Component.translatable("immersiveenchanting.mod_filter.selected",
                    title).withStyle(ChatFormatting.LIGHT_PURPLE);

        } else if(tooltip.node().isState(NodeState.UNOBTAINED)) {
            component = Component.translatable("immersiveenchanting.mod_filter.desc", title);
        }

        DescriptionHelper.lineWrapComponent(component, DescriptionHelper.DEFAULT_LINE_WIDTH, description, 0);
    }
}
