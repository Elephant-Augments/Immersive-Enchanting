package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.api.filter.FilterBranches;
import me.alfie.immersiveenchanting.api.filter.IsolatedFilterBranch;
import me.alfie.immersiveenchanting.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

/**
 * Applies title colors to enchantments from isolated filters with a registered color.
 *
 * <p>Only tints when the global client toggle is on and the enchantment's isolated 
 * branch registers a color. Obfuscation for locked GUI entries is handled by callers 
 * and takes precedence.</p>
 */
public final class EnchantmentTooltipColors {

    private EnchantmentTooltipColors() {}

    /**
     * Resolves the title color for an enchantment, if any override applies.
     */
    public static Optional<ChatFormatting> resolveColor(Holder<Enchantment> enchantment) {
        if(enchantment == null) {
            return Optional.empty();
        }
        if(enchantment.is(EnchantmentTags.CURSE)) {
            return Optional.of(ChatFormatting.RED);
        }
        if(!ClientConfig.isColorEnchantmentTooltipsByFilterEnabled()) {
            return Optional.empty();
        }
        return FilterBranches.resolveIsolatedBranch(enchantment)
                .flatMap(IsolatedFilterBranch::enchantmentTitleColor);
    }

    /**
     * Returns {@code name} restyled with the relevant filter/curse color, or unchanged when none applies.
     */
    public static Component styleEnchantmentName(Holder<Enchantment> enchantment, Component name) {
        Optional<ChatFormatting> color = resolveColor(enchantment);
        if(color.isEmpty()) {
            return name;
        }
        return name.copy().withStyle(color.get());
    }
}
