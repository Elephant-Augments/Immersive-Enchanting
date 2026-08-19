package me.alfie.immersiveenchanting.tags;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Enchantment tags used for special filter branches.
 * 
 * <p>Other mods can opt-in to automatically register 
 * their enchantments into these filters.</p>
 */
public final class ModEnchantmentTags {

    public static final TagKey<Enchantment> COSMETICS = TagKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "cosmetics")
    );

    private ModEnchantmentTags() {}

    public static boolean isCosmetic(Holder<Enchantment> enchantment) {
        return enchantment.is(COSMETICS);
    }

    public static boolean hasCosmetics(Iterable<Holder<Enchantment>> enchantments) {
        for (Holder<Enchantment> enchantment : enchantments) {
            if (isCosmetic(enchantment)) {
                return true;
            }
        }
        return false;
    }
}
