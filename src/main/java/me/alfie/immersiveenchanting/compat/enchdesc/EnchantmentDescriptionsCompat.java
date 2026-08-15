package me.alfie.immersiveenchanting.compat.enchdesc;

import me.alfie.immersiveenchanting.util.EnchantmentDescriptionHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Consumer;

/**
 * Optional compatibility with Enchantment Descriptions.
 *
 * <p>Descriptions for Ancient Books are injected via {@code ItemStackMixin}
 * using the same {@code enchantment.<namespace>.<path>.desc} keys.</p>
 */
public final class EnchantmentDescriptionsCompat {

    private EnchantmentDescriptionsCompat() {}

    public static void appendDescription(Holder<Enchantment> enchantment, Consumer<Component> consumer) {
        Component description = EnchantmentDescriptionHelper.getDescription(enchantment);
        if (description != null) {
            consumer.accept(description);
        }
    }
}
