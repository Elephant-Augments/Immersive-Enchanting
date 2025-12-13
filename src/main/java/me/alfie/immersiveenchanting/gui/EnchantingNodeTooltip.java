package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EnchantingNodeTooltip {
    private static final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath(
            ImmersiveEnchanting.MODID,
            "textures/gui/sprites/widgets.png"
    );

    private final int padding = 8;
    private final int iconSize = 26;
    private final int costIconSize = 16;
    private Font font;
    private EnchantingNode node;
    private String enchantmentName;
    private ItemStack costStack;
    private EnchantingTableScreen screen;
    private boolean isXFlip;
    private boolean isYFlip;

    private int tooltipWidth;
    private int tooltipHeight;

    private int startX;
    private int startY;

    private int costBoxHeight;

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
        tooltipWidth = (int) (padding/2 + textWidth + EnchantingNode.width);
        tooltipHeight = (int) Math.max(font.lineHeight, EnchantingNode.height);

        costBoxHeight = tooltipHeight-iconSize/2 + costIconSize + padding;;
    }


    public void renderEnchantmentTooltip(GuiGraphics graphics) {
        startX = node.getRenderedPosition(screen).x;
        startY = node.getRenderedPosition(screen).y;

        isXFlip = false;
        isYFlip = false;

        //Normal rendering is right to left
        if( node.getViewportPosition(screen).x + tooltipWidth > screen.viewportWidth) {
            startX = node.getRenderedPosition(screen).x - tooltipWidth + iconSize - 2;
            isXFlip = true;
        }

        //Normal rendering is top to bottom
        if( node.getViewportPosition(screen).y + costBoxHeight + tooltipHeight/2 - padding/2 > screen.viewportHeight) {
            isYFlip = true;
        }

        String label;
        int uvOffsetY;
        if(node.isObtained()) {
            uvOffsetY = 0;
            label = Component.translatable("gui.immersiveenchanting.equipped").getString();
        } else {
            uvOffsetY = 26;
            label = Component.translatable("gui.immersiveenchanting.cost").getString();
        }

        drawEnchantmentNameBox(uvOffsetY, graphics);
        drawCostBox(label, graphics);
    }

    /**
     * 1.20.1 Backport. Nine Slicing uses a 256x256 texture atlas not individual sprites.
     * Instead of resource location, passing the uv offset for the texture on the atlas.
     * @param uvOffsetY
     * @param graphics
     */
    private void drawEnchantmentNameBox(int uvOffsetY, GuiGraphics graphics) {
        graphics.blitNineSliced(WIDGETS, startX + 1, startY, tooltipWidth, tooltipHeight, 10, 200, 26, 0, uvOffsetY);


        //Enchantment name x depends on side
        int enchantmentLabelX = startX + padding/2;
        if(!isXFlip) {
            enchantmentLabelX += iconSize - padding/2;
        }

        //For y, the title/name boxes have to be flipped

        graphics.drawString(font, enchantmentName, enchantmentLabelX, startY + padding -1, 0xFFFFFF);
    }

    private void drawCostBox(String label, GuiGraphics graphics) {
        int costBoxX = startX + 1;
        int costBoxY = startY + tooltipHeight/2;

        if(isYFlip) {
            costBoxY = startY - tooltipHeight-padding/2;
        }


        final int costBoxLabelY = costBoxY+tooltipHeight/2;


        //graphics.blitSprite(TITLE_BOX_TEXTURE, costBoxX, costBoxY, 0, tooltipWidth, costBoxHeight);
        graphics.blitNineSliced(WIDGETS, costBoxX, costBoxY, tooltipWidth, costBoxHeight, 10, 200, 26, 0, 52);


        int costBoxLabelX = font.width(label);
        if(!node.isObtained() && costStack != null) {
            graphics.drawString(font, label, costBoxX + padding, costBoxLabelY + padding / 2, 0x55FF55);
            if (costStack.is(Items.AIR) || costStack.isEmpty()) {
                graphics.drawString(font, Component.translatable("gui.immersiveenchanting.cost_free"), costBoxX + costBoxLabelX + padding, costBoxLabelY + padding / 2, 0x55FF55); // optional green color
            } else {
                graphics.renderItem(costStack, costBoxX + costBoxLabelX + padding, costBoxLabelY);
                graphics.renderItemDecorations(font, costStack, costBoxX + costBoxLabelX + padding, costBoxLabelY);
            }
        } else if (costStack == null) {
            Component hint = Component.translatable("gui.immersiveenchanting.locked_enchantment_hint")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.OBFUSCATED);
            graphics.drawString(font, hint, costBoxX+padding/2, costBoxLabelY+padding/2, 0x55FF55);
        }

        else if(node.isObtained()) {
            graphics.drawString(font, label, costBoxX + padding, costBoxLabelY + padding / 2, 0x55FF55);
        }
    }
}
