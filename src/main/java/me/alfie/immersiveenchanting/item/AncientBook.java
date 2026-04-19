package me.alfie.immersiveenchanting.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.datafix.fixes.ItemStackEnchantmentNamesFix;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class AncientBook extends Item {

    public AncientBook(Properties properties) {
        super(properties
                .stacksTo(16)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        return true;
    }
}
