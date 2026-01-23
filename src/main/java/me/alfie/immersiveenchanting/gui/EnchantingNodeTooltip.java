package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.gui.tooltip.RenderDirection;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipDescription;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipTitle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector2i;

public class EnchantingNodeTooltip {

    private static final ResourceLocation BOX_UNOBTAINED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "box_unobtained");
    private static final ResourceLocation BOX_OBTAINED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "box_obtained");




    private static final ResourceLocation DESCRIPTION_BOX_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "title_box");

    private final int padding = 8;
    private final int iconSize = 26;
    private final int costIconSize = 16;
    private final Font font;
    public final EnchantingNode node;
    private final ItemStack costStack;
    public final EnchantingTableScreen screen;
    RenderDirection renderDirection;
    private Vector2i costStackPos = new Vector2i(0, 0);
    private int titleBoxWidth;
    private int titleBoxHeight;
    private int descriptionBoxHeight;
    private Vector2i titleBoxTopLeft = new Vector2i(0, 0);
    private Vector2i descriptionBoxTopLeft = new Vector2i(0, 0);

    private final TooltipTitle tooltipTitle;
    private final TooltipDescription tooltipDescription;

    public EnchantingNodeTooltip(Font font,
                                 EnchantingNode node,
                                 ItemStack costStack,
                                 EnchantingTableScreen screen) {
        this.font = font;
        this.node = node;
        this.costStack = costStack;
        this.screen = screen;

        //Create components
        tooltipTitle = new TooltipTitle(this, BOX_UNOBTAINED_TEXTURE);
        tooltipDescription = new TooltipDescription(this, DESCRIPTION_BOX_TEXTURE);
    }

    public ItemStack getCostStack() {
        return costStack;
    }

    public void renderEnchantmentTooltip(GuiGraphics graphics) {
        //Decide title text
        String titleText;
        if (node.isBranchUnlocked) {
            titleText = Enchantment.getFullname(node.getEnchantmentHolder(), node.getEnchantmentLevel())
                    .copy() // creates a mutable copy
                    .withStyle(ChatFormatting.WHITE)
                    .getString();
        } else {
            titleText = Component.translatable("gui.immersiveenchanting.locked_enchantment").withStyle(ChatFormatting.RED).getString();
        }

        //Setup positions/dimensions
        int textWidth = font.width(titleText);
        final int MIN_WIDTH = 64;
        titleBoxWidth = Math.max(padding / 2 + textWidth + EnchantingNode.width,
                MIN_WIDTH);

        titleBoxHeight = Math.max(font.lineHeight, EnchantingNode.height);
        descriptionBoxHeight = titleBoxHeight - iconSize / 2 + costIconSize + padding;

        //Build description layout
        tooltipDescription.buildLayout();

        //Set sizes
        tooltipTitle.setBoxSize(titleBoxWidth, titleBoxHeight);
        tooltipDescription.setBoxSize(tooltipTitle.getBoxWidth(), tooltipDescription.layout.getTotalHeight() + 20);
        setRenderDirection(); //Must call after setting sizes, but before setting positions!

        //Set positions
        tooltipTitle.setPos(titleBoxTopLeft.x+1, titleBoxTopLeft.y);
        tooltipDescription.setPos(descriptionBoxTopLeft.x, descriptionBoxTopLeft.y);

        //Title Settings
        ResourceLocation titleTexture = node.isObtained() ? BOX_OBTAINED_TEXTURE : BOX_UNOBTAINED_TEXTURE;
        tooltipTitle.setSpriteTexture(titleTexture);
        tooltipTitle.setTitleText(titleText);

        //Draw
        tooltipDescription.draw(graphics);
        tooltipTitle.draw(graphics);
    }

    /**
     * If tooltip will render out of bounds, this function will set the render direction accordingly.
     * Automatically sets the correct positions for enchantmentNameBoxTopLeft and costBoxTopLeft.
     */
    private void setRenderDirection() {
        boolean flipX = node.getViewportPosition(screen).x + tooltipTitle.getBoxWidth() > screen.VIEWPORT_WIDTH;
        boolean flipY = node.getViewportPosition(screen).y + tooltipDescription.getBoxHeight() + tooltipTitle.getBoxHeight() / 2 - padding / 2 > screen.VIEWPORT_HEIGHT;
        if (flipX && flipY) renderDirection = RenderDirection.LEFT_UP;
        else if (flipX) renderDirection = RenderDirection.LEFT_DOWN;
        else if (flipY) renderDirection = RenderDirection.RIGHT_UP;
        else renderDirection = RenderDirection.RIGHT_DOWN;

        int baseX = node.getRenderedPosition(screen).x;
        int baseY = node.getRenderedPosition(screen).y;
        switch (renderDirection) {
            case RIGHT_DOWN -> {
                titleBoxTopLeft = new Vector2i(baseX, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, titleBoxTopLeft.y + titleBoxHeight / 2);
            }

            case LEFT_DOWN -> {
                titleBoxTopLeft = new Vector2i(baseX - titleBoxWidth + iconSize - 2, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, titleBoxTopLeft.y + titleBoxHeight / 2);
            }

            case RIGHT_UP -> {
                titleBoxTopLeft = new Vector2i(baseX, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, (titleBoxTopLeft.y + titleBoxHeight / 2) - tooltipDescription.getBoxHeight() - padding/2);
            }

            case LEFT_UP -> {
                titleBoxTopLeft = new Vector2i(baseX - titleBoxWidth + iconSize - 2, baseY);
                descriptionBoxTopLeft = new Vector2i(titleBoxTopLeft.x + 1, (titleBoxTopLeft.y + titleBoxHeight / 2) - tooltipDescription.getBoxHeight() - padding/2);
            }
        }
    }

    public void setCostStackPos(int x, int y) {
        costStackPos = new Vector2i(x, y);
    }

    public Vector2i getCostStackPos() {
        return costStackPos;
    }


    public RenderDirection getRenderDirection() {
        return this.renderDirection;
    }

    public Font getFont() {
        return this.font;
    }

    public TooltipTitle getTooltipTitle() {
        return tooltipTitle;
    }

}
