package me.alfie.immersiveenchanting.datapack;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;

public record EnchantmentCostHolder(List<EnchantmentCost> costs) {

    public static final Codec<EnchantmentCostHolder> CODEC = Codec.list(
            EnchantmentCost.CODEC)
            .xmap(
                    EnchantmentCostHolder::new,
                    EnchantmentCostHolder::costs
            );
}
