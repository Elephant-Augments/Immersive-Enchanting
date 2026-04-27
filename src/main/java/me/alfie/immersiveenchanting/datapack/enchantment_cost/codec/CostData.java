package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.networking.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;

public record CostData(boolean enabled, CostLevels levelCosts) {

    public static final Codec<CostData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(CostData::enabled),
                    CostLevels.CODEC.fieldOf("levels").forGetter(CostData::levelCosts)
            ).apply(instance, CostData::new));

    public static final StreamCodec<CostData> STREAM_CODEC = new StreamCodec<CostData>() {
        @Override
        public void encode(FriendlyByteBuf buf, CostData value) {
            buf.writeBoolean(value.enabled());
            CostLevels.STREAM_CODEC.encode(buf, value.levelCosts());
        }

        @Override
        public CostData decode(FriendlyByteBuf buf) {
            boolean enabled = buf.readBoolean();
            CostLevels levelCosts = CostLevels.STREAM_CODEC.decode(buf);

            return new CostData(enabled, levelCosts);
        }
    };

}
