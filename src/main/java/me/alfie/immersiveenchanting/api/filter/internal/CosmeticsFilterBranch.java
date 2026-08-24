package me.alfie.immersiveenchanting.api.filter.internal;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.filter.IsolatedFilterBranch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

/** Built-in isolated filter: One branch showing all enchantments tagged as cosmetic ({@code #immersiveenchanting:cosmetics}). */
public final class CosmeticsFilterBranch implements IsolatedFilterBranch {

    public static final ResourceId ID = new ResourceId(ImmersiveEnchanting.MODID, "cosmetics");

    public static final TagKey<Enchantment> TAG = TagKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "cosmetics")
    );

    @Override
    public ResourceId id() {
        return ID;
    }

    @Override
    public TagKey<Enchantment> enchantmentTag() {
        return TAG;
    }

    @Override
    public Component title() {
        return Component.translatable("immersiveenchanting.mod_filter.cosmetics");
    }

    @Override
    public ItemStack createIconStack() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }
}
