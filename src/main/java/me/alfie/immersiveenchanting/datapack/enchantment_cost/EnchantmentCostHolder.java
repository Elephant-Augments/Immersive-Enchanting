package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record EnchantmentCostHolder(List<EnchantmentCost> costs) {

    public static final Codec<EnchantmentCostHolder> CODEC = Codec.list(
            EnchantmentCost.CODEC)
            .xmap(
                    EnchantmentCostHolder::new,
                    EnchantmentCostHolder::costs
            );

    public List<ItemStack> getAllItemStacks() {
        List<ItemStack> result = new ArrayList<>();
        for(EnchantmentCost cost : costs()) {
            result.addAll(cost.getItemStacks());
        }

        return result;
    }
}
