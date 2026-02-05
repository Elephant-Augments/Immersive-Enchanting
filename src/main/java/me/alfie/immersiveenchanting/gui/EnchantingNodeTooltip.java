package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.gui.tooltip.RenderDirection;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipDescription;
import me.alfie.immersiveenchanting.gui.tooltip.TooltipTitle;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector2i;

public class EnchantingNodeTooltip extends NodeTooltip {

    private final int costIconSize = 16;
    private final ItemStack costStack;
    private Vector2i costStackPos = new Vector2i(0, 0);

    public EnchantingNodeTooltip(EnchantingNode node,
                                 ItemStack costStack,
                                 EnchantingTableScreen screen) {
        super(node, screen);
        this.costStack = costStack;

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

        tooltipTitle.setTitleText(titleText);
    }

    public ItemStack getCostStack() {
        return costStack;
    }

    public void setCostStackPos(int x, int y) {
        costStackPos = new Vector2i(x, y);
    }

    public Vector2i getCostStackPos() {
        return costStackPos;
    }




}
