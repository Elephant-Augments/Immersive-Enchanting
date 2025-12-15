package me.alfie.immersiveenchanting.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector2i;

public class EnchantingNodeTooltip {

    private static final ResourceLocation BOX_UNOBTAINED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "box_unobtained");
    private static final ResourceLocation BOX_OBTAINED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "box_obtained");

    private static final ResourceLocation TITLE_BOX_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "title_box");

    private final int padding = 8;
    private final int iconSize = 26;
    private final int costIconSize = 16;
    private Font font;
    private EnchantingNode node;
    private String enchantmentName;
    private ItemStack costStack;
    private EnchantingTableScreen screen;

    private int titleBoxWidth;
    private int titleBoxHeight;
    private int descriptionBoxHeight;

    private Vector2i titleBoxTopLeft = new Vector2i(0,0);
    private Vector2i descriptionBoxTopLeft = new Vector2i(0,0);

    private enum RenderDirection {
        RIGHT_DOWN(false, false), //default: left to right, title on top, cost on bottom
        LEFT_DOWN(true, false), //right to left, title on top, cost on bottom
        RIGHT_UP(false, true), //left to right, cost on top, title on bottom
        LEFT_UP(true, true); //right to left, cost on top, title on bottom

        private final boolean flippedX; //Is x flipped from default (not left to right)
        private final boolean flippedY; //Is y flipped from default (not top to bottom)
        RenderDirection(boolean flippedX, boolean flippedY) {
            this.flippedX = flippedX;
            this.flippedY = flippedY;
        }

        /**
         * Is the render direction horizontally flipped (right to left instead of default left to right)?
         * @return
         */
        public boolean isFlippedX() {
            return flippedX;
        }

        /**
         * Is the render direction vertically flipped (cost on top, title on bottom instead of default title on top, cost on bottom)?
         * @return
         */
        public boolean isFlippedY() {
            return flippedY;
        }
    }
    RenderDirection renderDirection;

    public EnchantingNodeTooltip(Font font,
                                 EnchantingNode node,
                                 String enchantmentName,
                                 ItemStack costStack,
                                 EnchantingTableScreen screen) {
        this.font = font;
        this.node = node;
        this.enchantmentName = enchantmentName;
        this.costStack = costStack;
        this.screen = screen;

        //Setup positions
        int textWidth = font.width(enchantmentName);
        titleBoxWidth = (int) (padding/2 + textWidth + EnchantingNode.width);
        titleBoxHeight = (int) Math.max(font.lineHeight, EnchantingNode.height);

        descriptionBoxHeight = titleBoxHeight -iconSize/2 + costIconSize + padding;;
    }


    public void renderEnchantmentTooltip(GuiGraphics graphics) {
        setRenderDirection();


        drawTitleBox(enchantmentName, graphics);
        drawDescriptionBox(graphics);
    }

    /**
     * If tooltip will render out of bounds, this function will set the render direction accordingly.
     * Automatically sets the correct positions for enchantmentNameBoxTopLeft and costBoxTopLeft.
     */
    private void setRenderDirection() {
        boolean flipX = node.getViewportPosition(screen).x + titleBoxWidth > screen.viewportWidth;
        boolean flipY = node.getViewportPosition(screen).y + descriptionBoxHeight + titleBoxHeight /2 - padding/2 > screen.viewportHeight;
        if (flipX && flipY) renderDirection = RenderDirection.LEFT_UP;
        else if (flipX) renderDirection = RenderDirection.LEFT_DOWN;
        else if (flipY) renderDirection = RenderDirection.RIGHT_UP;
        else renderDirection = RenderDirection.RIGHT_DOWN;

        int baseX = node.getRenderedPosition(screen).x;
        int baseY = node.getRenderedPosition(screen).y;
        switch(renderDirection) {
            case RIGHT_DOWN -> {
                titleBoxTopLeft = new Vector2i(baseX, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, titleBoxTopLeft.y + titleBoxHeight /2);
            }

            case LEFT_DOWN -> {
                titleBoxTopLeft = new Vector2i(baseX - titleBoxWidth +iconSize-2, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, titleBoxTopLeft.y + titleBoxHeight /2);
            }

            case RIGHT_UP -> {
                titleBoxTopLeft = new Vector2i(baseX, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, titleBoxTopLeft.y - titleBoxHeight -padding/2);
            }

            case LEFT_UP -> {
                titleBoxTopLeft = new Vector2i(baseX - titleBoxWidth +iconSize-2, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, titleBoxTopLeft.y - titleBoxHeight -padding/2);
            }
        }
    }

    private void drawTitleBox(String titleText,
                              GuiGraphics graphics) {
        //The enchantment name box always stays at the same y position, only flips horizontally.
        ResourceLocation nineSlicedTexture = node.isObtained() ?
                BOX_OBTAINED_TEXTURE
                : BOX_UNOBTAINED_TEXTURE;

        graphics.blitSprite(nineSlicedTexture, titleBoxTopLeft.x + 1, titleBoxTopLeft.y, 0, titleBoxWidth, titleBoxHeight);

        final int titleTextX = titleBoxTopLeft.x + padding/2
                + (renderDirection.isFlippedX() ? 0 : iconSize - padding/2); //Add offset if renderDirection is left to right (makes room for the node)

        //Draw contents
        graphics.drawString(font,
                titleText,
                titleTextX,
                titleBoxTopLeft.y + padding -1,
                ChatFormatting.WHITE.getColor());
    }


    private void drawDescriptionBox(GuiGraphics graphics) {
        graphics.blitSprite(TITLE_BOX_TEXTURE, descriptionBoxTopLeft.x, descriptionBoxTopLeft.y, 0, titleBoxWidth, descriptionBoxHeight);

        String hintLabel = node.isObtained() ?
                Component.translatable("gui.immersiveenchanting.equipped").getString()
                : Component.translatable("gui.immersiveenchanting.cost").getString();

        final int costBoxLabelX = font.width(hintLabel);
        final int costBoxLabelY = descriptionBoxTopLeft.y+ titleBoxHeight /2;

        //Draw contents
        if(costStack != null) { //Is there a costStack to render?
            graphics.drawString(font,
                    hintLabel,
                    descriptionBoxTopLeft.x + padding,
                    costBoxLabelY + padding / 2,
                    ChatFormatting.GREEN.getColor());

            if(!node.isObtained()) {
                if (costStack.is(Items.AIR) || costStack.isEmpty()) {
                    graphics.drawString(font,
                            Component.translatable("gui.immersiveenchanting.cost_free"),
                            descriptionBoxTopLeft.x + costBoxLabelX + padding,
                            costBoxLabelY + padding / 2,
                            ChatFormatting.GREEN.getColor()); // optional green color
                } else {
                    graphics.renderItem(
                            costStack,
                            descriptionBoxTopLeft.x + costBoxLabelX + padding,
                            costBoxLabelY);
                    graphics.renderItemDecorations(font,
                            costStack,
                            descriptionBoxTopLeft.x+costBoxLabelX+padding,
                            costBoxLabelY);
                }
            }

        } else {
            Component hint = Component.translatable("gui.immersiveenchanting.locked_enchantment_hint")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.OBFUSCATED);
            graphics.drawString(font, hint, descriptionBoxTopLeft.x+padding/2, costBoxLabelY+padding/2, 0x55FF55);
        }


    }
}
