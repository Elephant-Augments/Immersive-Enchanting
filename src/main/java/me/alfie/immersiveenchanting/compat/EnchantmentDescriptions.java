package me.alfie.immersiveenchanting.compat;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.Nullable;

/**
 * EnchantmentDescriptions does not expose an API to fetch the raw component for a description
 */
public class EnchantmentDescriptions {

    //Not exposed by EnchDesc.
    private static final String[] KEY_TYPES = {"desc", "description", "info"};

    /**
     * Taken from EnchDesc as method is not exposed.
     * @param enchantment
     * @param id
     * @param level
     * @return
     */
    @Nullable
    protected static MutableComponent getDescription(Holder<Enchantment> enchantment, ResourceLocation id, int level) {
        MutableComponent description = getDescription("enchantment." + id.getNamespace() + "." + id.getPath() + ".", level);
        if (description == null && enchantment.value().description().getContents() instanceof TranslatableContents translatable) {
            description = getDescription(translatable.getKey() + ".", level);
        }
        return description;
    }

    /**
     * Taken from EnchDesc as method is not exposed.
     * @param baseKey
     * @param level
     * @return
     */
    @Nullable
    private static MutableComponent getDescription(String baseKey, int level) {
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
