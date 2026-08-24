package me.alfie.immersiveenchanting.api.description.internal;

import me.alfie.immersiveenchanting.api.description.DescriptionHelper;
import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.DescriptionLayoutExtension;
import me.alfie.immersiveenchanting.api.node.internal.FilterNodeData;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeState;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ModFilterLayoutExtension implements DescriptionLayoutExtension {
    @Override
    public void extendLayout(DescriptionLayout description, NodeTooltip tooltip) {
        if(!tooltip.node().isDataType(FilterNodeData.TYPE)) return;
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
