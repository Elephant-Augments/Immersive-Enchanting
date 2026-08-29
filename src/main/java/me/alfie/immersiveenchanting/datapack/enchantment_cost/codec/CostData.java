package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.alfinolib.networking.codec.CommonCodecs;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Optional;

public record CostData(boolean enabled, CostLevels levelCosts, Optional<ResourceLocation> dependsOn) {

    public static final Codec<CostData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(CostData::enabled),
                    CostLevels.CODEC.fieldOf("levels").forGetter(CostData::levelCosts),
                    ResourceLocation.CODEC.optionalFieldOf("depends_on").forGetter(CostData::dependsOn)
            ).apply(instance, CostData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CostData> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(RegistryFriendlyByteBuf buf, CostData data) {
                    CommonCodecs.BOOL.encode(buf, data.enabled());
                    CostLevels.STREAM_CODEC.encode(buf, data.levelCosts());
                    buf.writeBoolean(data.dependsOn().isPresent());
                    data.dependsOn().ifPresent(buf::writeResourceLocation);
                }

                @Override
                public CostData decode(RegistryFriendlyByteBuf buf) {
                    boolean enabled = CommonCodecs.BOOL.decode(buf);
                    CostLevels levels = CostLevels.STREAM_CODEC.decode(buf);
                    Optional<ResourceLocation> dependsOn = buf.readBoolean()
                            ? Optional.of(buf.readResourceLocation())
                            : Optional.empty();
                    return new CostData(enabled, levels, dependsOn);
                }
            };

    public static final CostData EMPTY = new CostData(true, new CostLevels(new HashMap<>()), Optional.empty());

    /** Convenience for callers that only set enabled + levels. */
    public CostData(boolean enabled, CostLevels levelCosts) {
        this(enabled, levelCosts, Optional.empty());
    }
}
