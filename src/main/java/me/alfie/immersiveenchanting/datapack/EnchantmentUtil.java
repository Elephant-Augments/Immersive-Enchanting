package me.alfie.immersiveenchanting.datapack;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentUtil {
    public static List<Holder<Enchantment>> idsToHolders(List<Identifier> ids, RegistryAccess access) {
        List<Holder<Enchantment>> result = new ArrayList<>();
        for(Identifier id : ids) {
            result.add(toHolder(id, access));
        }
        return result;
    }

    public static Holder<Enchantment> toHolder(Identifier id, RegistryAccess access) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).get(id).orElseThrow();
    }

    public static Identifier toId(Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder.unwrapKey().orElseThrow().identifier();
    }

}
