package me.alfie.immersiveenchanting.util;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Locale;

/**
 * Shared enchantment search matching for the book list and enchanting tree.
 */
public final class EnchantmentSearchHelper {

    private EnchantmentSearchHelper() {}

    /**
     * @return {@code true} if {@code query} is empty or matches the enchantment name/tooltip text.
     */
    public static boolean matches(Holder<Enchantment> enchantment, String query) {
        if(query.isEmpty()) {
            return true;
        }

        boolean descriptionsOnly = query.startsWith("$");
        String term = descriptionsOnly ? query.substring(1) : query;
        if(term.isEmpty()) {
            return true;
        }

        String lowerQuery = term.toLowerCase(Locale.ROOT);

        if(!descriptionsOnly) {
            String name = enchantment.value().description().getString().toLowerCase(Locale.ROOT);
            if(name.contains(lowerQuery)) {
                return true;
            }
        }

        return matchesTooltipText(enchantment, lowerQuery);
    }

    private static boolean matchesTooltipText(Holder<Enchantment> enchantment, String lowerQuery) {
        Component enchantDescription = EnchantmentDescriptionHelper.getDescription(enchantment);
        if(enchantDescription != null && containsNormalized(enchantDescription.getString(), lowerQuery)) {
            return true;
        }

        ResourceLocation id = enchantment.unwrapKey()
                .map(key -> key.location())
                .orElse(null);
        if(id == null) {
            return false;
        }

        String spellNameKey = "spell." + id.getNamespace() + "." + id.getPath() + ".name";
        if(I18n.exists(spellNameKey) && containsNormalized(I18n.get(spellNameKey), lowerQuery)) {
            return true;
        }

        String spellDescriptionKey = "spell." + id.getNamespace() + "." + id.getPath() + ".description";
        return I18n.exists(spellDescriptionKey)
                && containsNormalized(I18n.get(spellDescriptionKey), lowerQuery);
    }

    /** Strips spell-engine style {@code {tokens}} before substring matching. */
    private static boolean containsNormalized(String text, String lowerQuery) {
        return normalizeSearchText(text).contains(lowerQuery);
    }

    private static String normalizeSearchText(String text) {
        return text.replaceAll("\\{[^}]*\\}", "").toLowerCase(Locale.ROOT);
    }
}
