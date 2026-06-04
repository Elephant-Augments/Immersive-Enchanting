package me.alfie.immersiveenchanting.util;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class EnchantmentUtil {

    public static Holder<Enchantment> toHolder(ResourceId id, RegistryAccess access) {
        return access.lookupOrThrow(Registries.ENCHANTMENT).get(id.mc()).orElseThrow();
    }

    public static Holder<Enchantment> toHolder(ResourceKey<Enchantment> enchantmentKey, RegistryAccess access) {
        return toHolder(ResourceId.parse(enchantmentKey.identifier().toString()), access);
    }

    public static ResourceId toId(Holder<Enchantment> enchantmentHolder) {
        return ResourceId.parse(enchantmentHolder.unwrapKey().orElseThrow().identifier().toString());
    }

    public static List<ResourceKey<Enchantment>> toResourceKeys(List<Holder<Enchantment>> enchantmentHolders) {
        List<ResourceKey<Enchantment>> result = new ArrayList<>(enchantmentHolders.size());
        for(Holder<Enchantment> enchantmentHolder : enchantmentHolders) {
            result.add(enchantmentHolder.getKey());
        }
        return result;
    }

    public static List<Holder<Enchantment>> toHolders(List<ResourceKey<Enchantment>> enchantmentKeys, RegistryAccess registryAccess) {
        List<Holder<Enchantment>> result = new ArrayList<>(enchantmentKeys.size());
        for(ResourceKey<Enchantment> enchantmentKey : enchantmentKeys) {
            result.add(toHolder(ResourceId.parse(enchantmentKey.identifier().toString()), registryAccess));
        }
        return result;
    }

    public static void setStoredEnchantment(ItemStack ancientBook, Holder<Enchantment> enchantmentHolder) {
        ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        itemEnchantments.set(enchantmentHolder, 1);
        ancientBook.set(DataComponents.STORED_ENCHANTMENTS, itemEnchantments.toImmutable());
    }

    public static @Nullable Holder<Enchantment> getStoredEnchantment(ItemStack ancientBook) {
        ItemEnchantments itemEnchantments = ancientBook.get(DataComponents.STORED_ENCHANTMENTS);
        if(itemEnchantments == null) return null;

        List<Holder<Enchantment>> enchantments = itemEnchantments.keySet().stream().toList();
        if(enchantments.isEmpty()) return null;

        return enchantments.getFirst();
    }

    public static void setReplicated(ItemStack ancientBook) {
        ancientBook.set(ModDataComponents.REPLICATED, new ReplicatedDataComponent(true));
    }

    public static boolean isReplicated(ItemStack ancientBook) {
        if(!ancientBook.has(ModDataComponents.REPLICATED)) return false;
        return ancientBook.get(ModDataComponents.REPLICATED.get()).isReplicated();
    }

    public static List<Holder<Enchantment>> getAllEnchantmentsInRegistry(HolderLookup.Provider lookup) {
        return lookup.lookupOrThrow(Registries.ENCHANTMENT).listElements().map(holder -> (Holder<Enchantment>) holder).toList();
    }
}
