package me.alfie.immersiveenchanting.gui.enchanting;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class EnchantingNodeTooltip extends NodeTooltip {


    public int lastBars = 0;

    public EnchantingNodeTooltip(EnchantingNode node,
                                 EnchantingTableScreen screen,
                                 List<CostEntry> validCosts) {
        super(node, screen, validCosts);

        //Decide title text
        Component titleText = Enchantment.getFullname(node.getEnchantmentHolder(), node.getEnchantmentLevel())
                .copy() // creates a mutable copy
                .withStyle(ChatFormatting.WHITE);
        if(!node.isBranchUnlocked) {
            if(ServerConfig.isObfuscateLockedEnchantments()) {
                titleText = ImmersiveEnchanting.getAltFont(titleText);
            }
        }


        tooltipTitle.setTitleText(titleText);
    }
}
