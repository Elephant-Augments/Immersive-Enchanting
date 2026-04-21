package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.api.description.DescriptionLayout;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.gui.core.NineSliceSprite;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class TooltipDescription extends TooltipComponent {

    private final NodeTooltip tooltip;
    private final DescriptionLayout layout;

    public TooltipDescription(NodeTooltip tooltip) {
        super(NineSliceSprite.TOOLTIP_DESCRIPTION.id());
        this.tooltip = tooltip;
        this.layout = new DescriptionLayout(this);
    }

    public void render(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        TooltipDescriptionExtensions.rebuild(tooltip, layout);

        int yOffset = Node.HEIGHT-10;
        setPos(x()+1, y()+yOffset);
        super.blitNineSliceSprite(graphics);

        setTextStartPos(x() + 4, y() + 12);
        layout.render(graphics, getTextStartPos().x(), getTextStartPos().y(), mouseX, mouseY);

        Sprite mouseSprite = tooltip.screen().tooltipManager().isTooltipLocked() ? Sprite.MOUSE_HINT_ON : Sprite.MOUSE_HINT_OFF;
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                mouseSprite.id(),
                x() + getWidth() - 10, y() + getHeight() - 14,
                0, 0,
                mouseSprite.width(), mouseSprite.height(),
                mouseSprite.width(), mouseSprite.height()
        );
    }

    public DescriptionLayout getDescriptionLayout() {
        return layout;
    }
}
