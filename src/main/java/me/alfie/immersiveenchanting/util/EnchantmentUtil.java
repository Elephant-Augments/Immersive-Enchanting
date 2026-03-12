package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class EnchantmentUtil {

    public static HolderLookup.RegistryLookup<Enchantment> getEnchantmentLookup(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
    }

    /**
     * Automatically get an enchantment holder using RegistryAccess.
     * @param access
     * @param enchantment
     * @return
     */
    public static Optional<Holder.Reference<Enchantment>> getEnchantmentHolder(RegistryAccess access, ResourceKey<Enchantment> enchantment) {
        return access.registryOrThrow(Registries.ENCHANTMENT).getHolder(enchantment);
    }

    /**
     * Returns all enchantments.
     * @param level
     * @param filterDisabledEnchantments if true, will return all enchantments including disabled ones.
     * @return
     */
    public static List<Holder.Reference<Enchantment>> getAllEnchantments(Level level, boolean filterDisabledEnchantments) {
        HolderLookup.RegistryLookup<Enchantment> lookup = getEnchantmentLookup(level.registryAccess());
        List<Holder.Reference<Enchantment>> allEnchantments = lookup.listElements().toList();

        //Filter disabled enchantments and curses
        return allEnchantments.stream()
                .filter(enchantment -> {
                    if(filterDisabledEnchantments) {
                        if (EnchantmentCostRegistry.getRegistry(level).getCostRegistry().containsKey(enchantment.key())) {
                            if (!EnchantmentCostRegistry.getRegistry(level).getCostRegistry().get(enchantment.key()).enabled) return false;
                        }
                    }

                    if (enchantment.value().isCurse()) return false;
                    return true;
                }).toList();
    }

    /**
     * Returns all enchantments that are enabled.
     * @param level
     * @return
     */
    public static List<Holder.Reference<Enchantment>> getAllEnchantments(Level level) {
        return getAllEnchantments(level, true);
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
