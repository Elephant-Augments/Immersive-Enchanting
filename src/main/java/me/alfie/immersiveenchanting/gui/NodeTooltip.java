package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.gui.tooltip.RenderDirection;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipDescription;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipTitle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector2i;

public class NodeTooltip {
    protected static final ResourceLocation BOX_UNOBTAINED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "box_unobtained");
    protected static final ResourceLocation BOX_OBTAINED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "box_obtained");
    protected static final ResourceLocation DESCRIPTION_BOX_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            "immersiveenchanting", "title_box");

    RenderDirection renderDirection;
    public final Node node;
    public final EnchantingTableScreen screen;

    protected final TooltipTitle tooltipTitle;
    protected final TooltipDescription tooltipDescription;

    protected final int iconSize = 26;
    protected final int padding = 8;
    protected int titleBoxWidth;
    protected int titleBoxHeight;
    protected Vector2i titleBoxTopLeft = new Vector2i(0, 0);
    protected Vector2i descriptionBoxTopLeft = new Vector2i(0, 0);

    private String titleText;

    public NodeTooltip(Node node, EnchantingTableScreen screen) {
        this.node = node;
        this.screen = screen;

        //Create components
        tooltipTitle = new TooltipTitle(this, BOX_UNOBTAINED_TEXTURE);
        tooltipDescription = new TooltipDescription(this, DESCRIPTION_BOX_TEXTURE);
    }

    public void render(GuiGraphics graphics) {
        titleBoxHeight = Math.max(Minecraft.getInstance().font.lineHeight, EnchantingNode.height);

        //Build description layout
        tooltipDescription.buildLayout();

        //Calculate width: Use title width, but if description extends it, go further.
        final int titleTextWidth = Minecraft.getInstance().font.width(tooltipTitle.getTitleText()) + EnchantingNode.width + padding/2;
        final int descriptionTextWidth = Minecraft.getInstance().font.width(tooltipDescription.layout.getLongestString()) + padding;
        titleBoxWidth = Math.max(titleTextWidth, descriptionTextWidth);

        //Set sizes
        tooltipTitle.setBoxSize(titleBoxWidth, titleBoxHeight);
        tooltipDescription.setBoxSize(tooltipTitle.getBoxWidth(), tooltipDescription.layout.getRenderedHeight() + 20);
        setRenderDirection(); //Must call after setting sizes, but before setting positions!

        //Set positions
        tooltipTitle.setPos(titleBoxTopLeft.x+1, titleBoxTopLeft.y);
        tooltipDescription.setPos(descriptionBoxTopLeft.x, descriptionBoxTopLeft.y);

        //Title Settings
        ResourceLocation titleTexture = node.isObtained() ? BOX_OBTAINED_TEXTURE : BOX_UNOBTAINED_TEXTURE;
        tooltipTitle.setSpriteTexture(titleTexture);

        //Draw
        tooltipDescription.draw(graphics);
        tooltipTitle.draw(graphics);
    }

    /**
     * If tooltip will render out of bounds, this function will set the render direction accordingly.
     * Automatically sets the correct positions for enchantmentNameBoxTopLeft and costBoxTopLeft.
     */
    protected void setRenderDirection() {
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

    public RenderDirection getRenderDirection() {
        return this.renderDirection;
    }

    public TooltipTitle getTooltipTitle() {
        return tooltipTitle;
    }

}
