package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.gui.NineSliceSprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TooltipDescription extends NineSliceBox {

    private final NodeTooltip nodeTooltip;

    public TooltipDescription(NodeTooltip nodeTooltip) {
        super(NineSliceSprite.TOOLTIP_DESCRIPTION.id());
        this.nodeTooltip = nodeTooltip;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        super.render(graphics);
    }
}
