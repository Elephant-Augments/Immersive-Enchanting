package me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.enchanting;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.cost.CostEntry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.NodeTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

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
                titleText = ImmersiveEnchanting.styleWithAltFont(titleText);
            }
        }


        tooltipTitle.setTitleText(titleText);
    }
}
