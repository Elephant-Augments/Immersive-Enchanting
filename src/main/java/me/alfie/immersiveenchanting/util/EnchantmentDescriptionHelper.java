package me.alfie.immersiveenchanting.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

/**
 * Adds enchantment description text using Enchantment Descriptions conventions.
 */
public final class EnchantmentDescriptionHelper {

    private EnchantmentDescriptionHelper() {}

    @Nullable
    public static Component getDescription(Holder<Enchantment> enchantment) {
        if (Minecraft.getInstance() == null) {
            return null;
        }

        String descriptionId = resolveDescriptionId(enchantment);
        if (descriptionId == null) {
            return null;
        }

        String descriptionKey = descriptionId + ".desc";

        if (!I18n.exists(descriptionKey) && I18n.exists(descriptionId + ".description")) {
            descriptionKey = descriptionId + ".description";
        }

        if (!I18n.exists(descriptionKey)) {
            return null;
        }

        return Component.translatable(descriptionKey).withStyle(ChatFormatting.DARK_GRAY);
    }

    @Nullable
    private static String resolveDescriptionId(Holder<Enchantment> enchantment) {
        var key = enchantment.getKey();
        if (key != null) {
            return "enchantment." + key.location().getNamespace() + "." + key.location().getPath();
        }

        Component name = enchantment.value().description();
        if (name.getContents() instanceof TranslatableContents translatable) {
            return translatable.getKey();
        }

        return null;
    }
}
