package me.alfie.immersiveenchanting.gui.tooltip;

import me.alfie.immersiveenchanting.api.TooltipExtensions;
import me.alfie.immersiveenchanting.gui.EnchantingNodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class TooltipDescription extends NineSliceBox {

    private static final ResourceLocation MOUSE_HINT_OFF_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "textures/gui/sprites/mouse_hint_off.png");
    private static final ResourceLocation MOUSE_HINT_ON_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "textures/gui/sprites/mouse_hint_on.png");

    public final DescriptionLayout layout;

    public TooltipDescription(EnchantingNodeTooltip parentTooltip, ResourceLocation spriteTexture) {
        super(parentTooltip, spriteTexture);
        layout = new DescriptionLayout(this);
    }

    /**
     * Change layout here - do not call directly in the draw() function, as you may see artifacts as the layout has updated late.
     */
    public void buildLayout() {
        layout.clear();

        //Draw label line (this takes up 2 lines due to the cost stack sprite)
        layout.insertLine(0, new DescriptionLine() {
            @Override
            public void draw(GuiGraphics graphics, int lineX, int lineY) {
                //Draw label
                graphics.drawString(Minecraft.getInstance().font,
                        getText(),
                        lineX,
                        lineY + 4, //Offset to centre text with cost stack
                        0xFFFFFF);

                //Draw cost stack if applicable
                if (parentTooltip.node.isBranchUnlocked && !parentTooltip.node.isObtained()) {
                    Vector2i costStackPos = new Vector2i(lineX + Minecraft.getInstance().font.width(getText()), lineY);
                    parentTooltip.setCostStackPos(costStackPos.x, costStackPos.y);
                    graphics.renderItem(
                            parentTooltip.getCostStack(),
                            costStackPos.x,
                            costStackPos.y);
                    graphics.renderItemDecorations(Minecraft.getInstance().font,
                            parentTooltip.getCostStack(),
                            costStackPos.x,
                            costStackPos.y);
                }
            }

            @Override
            public @NotNull Component getText() {
                Component label;
                if (parentTooltip.node.isBranchUnlocked) {
                    label = parentTooltip.node.isObtained() ?
                            Component.translatable("gui.immersiveenchanting.equipped").withStyle(ChatFormatting.LIGHT_PURPLE) :
                            Component.translatable("gui.immersiveenchanting.cost").withStyle(ChatFormatting.GREEN);
                } else {
                    label = Component.translatable("gui.immersiveenchanting.locked_enchantment_hint")
                            .withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.GRAY);
                }
                return label;
            }
        });

        TooltipExtensions.apply(parentTooltip,layout);
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
        ResourceLocation texture = !this.parentTooltip.screen.isLockHover() ? MOUSE_HINT_OFF_TEXTURE : MOUSE_HINT_ON_TEXTURE;
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
