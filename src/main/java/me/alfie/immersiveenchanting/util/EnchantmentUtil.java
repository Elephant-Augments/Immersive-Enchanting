package me.alfie.immersiveenchanting.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

public class EnchantmentUtil {

    public HolderLookup.RegistryLookup<Enchantment> getEnchantmentRegistryLookup(RegistryAccess registryAccess) {
        return registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
    }
}
