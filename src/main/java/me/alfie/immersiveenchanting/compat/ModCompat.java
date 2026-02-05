package me.alfie.immersiveenchanting.compat;

import net.enchant_limiter.api.LimitHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nullable;
import java.util.List;

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
     * Duplicated from Enchantment Descriptions as method is private.
     * @param enchantment
     * @param id
     * @param level
     * @return
     */
    @Nullable
    public static MutableComponent getDescription(Holder<Enchantment> enchantment, ResourceLocation id, int level) {
        MutableComponent description = getDescription("enchantment." + id.getNamespace() + "." + id.getPath() + ".", level);
        if (description == null && enchantment.value().description().getContents() instanceof TranslatableContents translatable) {
            description = getDescription(translatable.getKey() + ".", level);
        }

        return description;
    }

    /**
     * Duplicated from Enchantment Descriptions as method is private
     * @param baseKey
     * @param level
     * @return
     */
    @Nullable
    private static MutableComponent getDescription(String baseKey, int level) {
        final String[] KEY_TYPES = {"desc", "description", "info"};
        for (String keyType : KEY_TYPES) {
            String key = baseKey + keyType;
            if (I18n.exists(key)) {
                return Component.translatable(key);
            }
            key = key + "." + level;
            if (I18n.exists(key)) {
                return Component.translatable(key);
            }
        }
        return null;
    }
}
