package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.List;

public class EnchantmentUtil {

    public static HolderLookup.RegistryLookup<Enchantment> getEnchantmentLookup(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
    }

    /**
     * Returns all enchantments that are enabled.
     * @param level
     * @return
     */
    public static List<Holder.Reference<Enchantment>> getAllEnchantments(Level level) {
        HolderLookup.RegistryLookup<Enchantment> lookup = getEnchantmentLookup(level.registryAccess());
        List<Holder.Reference<Enchantment>> allEnchantments = lookup.listElements().toList();

        //Filter disabled enchantments and curses
        return allEnchantments.stream()
                .filter(enchantment -> {
                    if (EnchantmentCostRegistry.getServerRegistry().getCostRegistry().containsKey(enchantment.key())) {
                        if (!EnchantmentCostRegistry.getServerRegistry().getCostRegistry().get(enchantment.key()).enabled) return false;
                    }

                    if (enchantment.is(EnchantmentTags.CURSE)) return false;
                    return true;
                }).toList();
    }


    /**
     * Get a random enabled enchantment.
     * @param level
     * @param randomSource
     * @return
     */
    public static Holder<Enchantment> getRandomEnchantment(Level level, RandomSource randomSource) {
        List<Holder.Reference<Enchantment>> filteredEnchantments = getAllEnchantments(level);

        if (!filteredEnchantments.isEmpty()) {
            return filteredEnchantments.get(randomSource.nextInt(filteredEnchantments.size()));
        }
        return null;
    }
}
