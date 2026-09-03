package me.alfie.immersiveenchanting.compat.tooltip;

import me.alfie.immersiveenchanting.util.EnchantmentDescriptionHelper;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Repairs enchantment-description ordering after other mods change the tooltip list.
 *
 * <p>Simply runs at {@link EventPriority#LOWEST} and moves each description
 * back under its matching name to catch certain edge cases (like Spell Engine's
 * "Allows spell casting" insert).</p>
 */
public final class EnchantmentTooltipReorder {

    private EnchantmentTooltipReorder() {}

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        ItemEnchantments enchantments = EnchantmentUtil.getEnchantments(stack);
        if(enchantments == null || enchantments.isEmpty()) {
            return;
        }

        List<Component> lines = event.getToolTip();
        if(lines.isEmpty()) {
            return;
        }

        List<Holder<Enchantment>> holders = new ArrayList<>(enchantments.keySet());
        for(int i = holders.size() - 1; i >= 0; i--) {
            Holder<Enchantment> enchantment = holders.get(i);
            int level = enchantments.getLevel(enchantment);
            Component expectedName = Enchantment.getFullname(enchantment, level);
            Component expectedDesc = EnchantmentDescriptionHelper.getDescription(enchantment);
            if(expectedDesc == null) {
                continue;
            }

            String nameText = expectedName.getString();
            String descText = expectedDesc.getString();
            if(nameText.isEmpty() || descText.isEmpty()) {
                continue;
            }

            int nameIndex = -1;
            for(int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
                if(lines.get(lineIndex).getString().equals(nameText)) {
                    nameIndex = lineIndex;
                    break;
                }
            }
            if(nameIndex < 0) {
                continue;
            }

            int descIndex = -1;
            for(int lineIndex = nameIndex + 1; lineIndex < lines.size(); lineIndex++) {
                Component line = lines.get(lineIndex);
                if(isDescriptionLine(line, descText)) {
                    descIndex = lineIndex;
                    break;
                }
            }

            if(descIndex < 0 || descIndex == nameIndex + 1) {
                continue;
            }

            Component descriptionLine = lines.remove(descIndex);
            lines.add(nameIndex + 1, descriptionLine);
        }
    }

    /**
     * Restrict to dark-gray lines so green/blue attribute rows are never treated as a desc.
     */
    private static boolean isDescriptionLine(Component line, String descriptionText) {
        String lineText = line.getString();
        if(lineText.isEmpty()) {
            return false;
        }
        if(!lineText.equals(descriptionText) && !lineText.contains(descriptionText)) {
            return false;
        }
        return hasDarkGrayStyle(line);
    }

    private static boolean hasDarkGrayStyle(Component line) {
        if(isDarkGray(line)) {
            return true;
        }
        for(Component sibling : line.getSiblings()) {
            if(hasDarkGrayStyle(sibling)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDarkGray(Component component) {
        var color = component.getStyle().getColor();
        return color != null && color.getValue() == ChatFormatting.DARK_GRAY.getColor();
    }
}
