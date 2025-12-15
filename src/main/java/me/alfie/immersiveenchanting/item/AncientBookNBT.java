package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public class AncientBookNBT {
    public static final String NBT_ID = ImmersiveEnchanting.MODID + ":ancient_book_enchantment_type";

    /**
     * Set NBT containing enchantment type as a string
     *
     * @param bookStack
     * @param enchantmentHolder
     */
    public static void setEnchantment(ItemStack bookStack, Holder<Enchantment> enchantmentHolder) {
        CompoundTag tag = bookStack.getOrCreateTag(); //Get tag
        tag.putString(NBT_ID, enchantmentHolder.unwrapKey().get().location().toString()); //Store enchantment name as string
    }

    public static ResourceKey<Enchantment> getEnchantment(ItemStack bookStack) {
        if (bookStack.hasTag()) { // Check if the tag exists first
            CompoundTag tag = bookStack.getTag();
            if (tag != null) {
                return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(tag.getString(NBT_ID)));
            }
        }
        return null;
    }
}
