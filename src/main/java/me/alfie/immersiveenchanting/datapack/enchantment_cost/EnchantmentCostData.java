package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EnchantmentCostData(boolean enabled, EnchantmentCostLevels levelCosts) {

    public static final Codec<EnchantmentCostData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(EnchantmentCostData::enabled),
                    EnchantmentCostLevels.CODEC.fieldOf("levels").forGetter(EnchantmentCostData::levelCosts)
            ).apply(instance, EnchantmentCostData::new));
}
