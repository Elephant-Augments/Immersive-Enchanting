package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.alfinolib.networking.codec.CommonCodecs;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.alfinolib.networking.codec.StreamCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;

public record CostData(boolean enabled, CostLevels levelCosts) {

    public static final Codec<CostData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(CostData::enabled),
                    CostLevels.CODEC.fieldOf("levels").forGetter(CostData::levelCosts)
            ).apply(instance, CostData::new));

    public static final StreamCodec<FriendlyByteBuf, CostData> STREAM_CODEC =
            StreamCodecBuilder.<FriendlyByteBuf, CostData>create()
                    .add(CommonCodecs.BOOL, CostData::enabled)
                    .add(CostLevels.STREAM_CODEC, CostData::levelCosts)
                    .build(CostData::new);

    public static final CostData EMPTY = new CostData(true, new CostLevels(new HashMap<>()));
}
