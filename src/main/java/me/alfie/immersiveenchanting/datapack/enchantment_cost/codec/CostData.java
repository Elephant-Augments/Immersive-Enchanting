package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;

public record CostData(boolean enabled, CostLevels levelCosts) {

    public static final Codec<CostData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(CostData::enabled),
                    CostLevels.CODEC.fieldOf("levels").forGetter(CostData::levelCosts)
            ).apply(instance, CostData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CostData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, CostData::enabled,
            CostLevels.STREAM_CODEC, CostData::levelCosts,
            CostData::new
    );

    public static final CostData EMPTY = new CostData(true, new CostLevels(
            new HashMap<>()));
}
