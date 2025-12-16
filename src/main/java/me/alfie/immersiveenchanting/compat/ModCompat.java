package me.alfie.immersiveenchanting.compat;

import net.darkhax.enchdesc.common.impl.EnchdescMod;
import net.enchant_limiter.api.LimitHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.darkhax.enchdesc.common.api.ContextProvider;

import javax.annotation.Nullable;

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
     * Compatibility with EnchDesc.<br>
     * Returns the enchantment description in a MutableComponent for custom rendering.
     * @param enchantmentHolder
     * @param enchantmentRL
     * @param enchantmentLevel
     */
    public static MutableComponent getEnchantmentDescription(Holder<Enchantment> enchantmentHolder,
                                                 ResourceLocation enchantmentRL,
                                                 int enchantmentLevel) {
        if (ModCheck.Mod.ENCHANTMENT_DESCRIPTIONS.isLoaded()) {
            return EnchantmentDescriptions.getDescription(
                    enchantmentHolder,
                    enchantmentRL,
                    enchantmentLevel
            );
        }
        return null;
    }
}
