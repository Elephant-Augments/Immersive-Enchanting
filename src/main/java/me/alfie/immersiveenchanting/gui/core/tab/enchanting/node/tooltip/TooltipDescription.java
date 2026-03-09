package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.tooltip;

import me.alfie.immersiveenchanting.api.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector2i;

public class TooltipDescription extends NineSliceBox {

    private static final ResourceLocation MOUSE_HINT_OFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "textures/gui/sprites/mouse_hint_off.png");
    private static final ResourceLocation MOUSE_HINT_ON_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "textures/gui/sprites/mouse_hint_on.png");

    public final DescriptionLayout layout;

    public TooltipDescription(NodeTooltip parentTooltip, ResourceLocation spriteTexture) {
        super(parentTooltip, spriteTexture);
        layout = new DescriptionLayout(this);
    }

    /**
     * Change layout here - do not call directly in the draw() function, as you may see artifacts as the layout has updated late.
     */
    public void buildLayout() {
        layout.clear();

        TooltipDescriptionExtensions.apply(parentTooltip, layout);
    }

    @Override
    public void draw(GuiGraphics graphics) {
        super.draw(graphics);

        drawMouseHint(graphics);

        //Draw the DescriptionLayout (all lines)
        Vector2i startPos = new Vector2i(this.getX() + 4, this.getY() + 8);
        //If render direction is DOWN, icon blocks 1st line, move down slightly.
        int offset = parentTooltip.getRenderDirection().isFlippedY() ? 0 : 6;
        layout.draw(graphics, startPos.x, startPos.y + offset);
    }

    private void drawMouseHint(GuiGraphics graphics) {
        Vector2i boxBottomRight = new Vector2i(
                this.getX() + this.parentTooltip.getTooltipTitle().getBoxWidth(),
                this.getY() + this.getBoxHeight()
        );

        //Draw mouse right click hint
        ResourceLocation texture = !this.parentTooltip.screen.enchantingTab.isLockHover() ? MOUSE_HINT_OFF_TEXTURE : MOUSE_HINT_ON_TEXTURE;
        //If render direction is LEFT_UP, icon blocks icon, move up slightly.
        int offset = parentTooltip.getRenderDirection().equals(RenderDirection.LEFT_UP) ? 2 : 0;
        graphics.blit(
                texture,
                boxBottomRight.x - 10,
                boxBottomRight.y - 14 - offset,
                0f, 0f, 8, 8,
                8, 8
        );
    }
}
