package me.alfie.immersiveenchanting.compat;

import net.enchant_limiter.api.LimitHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import reliquary.data.ReliquaryEnchantmentProvider;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Compatibility helpers for other mods.<br>
 * Compatible mods are defined in enum {@code ModCheck.Mod}
 */
public class ModCompat {
    /**
     * Check if the enchantment can be applied to this item stack when a mod that limits or prevents enchantments is present.
     * @param stack
     * @param enchantment
     * @return
     */
    public static boolean canEnchant(final ItemStack stack, final Holder<Enchantment> enchantment) {
        if (ModCheck.Mod.ENCHANT_LIMITER.isLoaded()) {
            if (stack.getEnchantmentLevel(enchantment) > 0) {
                // Allow increasing the level of existing enchantments
                return true;
            }

            // The mod does not seem to exclude curses from the limit
            //noinspection deprecation -> same method used by the mod itself
            return stack.getEnchantments().size() < LimitHelper.getLimitCount(stack);
        }

        return true;
    }

    /**
     * Fixes a mod compat issue with reliquary.<br>
     * The Magicbane item overrides the getEnchantmentLevel function and always adds +2.<br>
     * This breaks the EnchantingTableScreen UI. This function adds -2 to the enchantmentLevel if the item is MagicBane.
     * @param currentItemStack
     * @param enchantmentHolder
     * @param enchantmentLevel
     */
    public static void reliquaryMagicbaneFix(ItemStack currentItemStack,
                                            Holder<Enchantment> enchantmentHolder,
                                            AtomicInteger enchantmentLevel) {
        if(currentItemStack.is(reliquary.init.ModItems.MAGICBANE.get())) {
            if(enchantmentHolder.is(ReliquaryEnchantmentProvider.SEVERING)) {
                enchantmentLevel.set(enchantmentLevel.get()-2);
            }
        }
    }
}
