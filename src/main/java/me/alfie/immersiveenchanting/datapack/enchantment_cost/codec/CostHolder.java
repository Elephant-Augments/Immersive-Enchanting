package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record CostHolder(List<Cost> costs) {

    public static final Codec<CostHolder> CODEC = Codec.list(
            Cost.CODEC)
            .xmap(
                    CostHolder::new,
                    CostHolder::costs
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, CostHolder> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Cost.STREAM_CODEC),
            CostHolder::costs,
            CostHolder::new);

    public List<ItemStack> getAllItemStacks() {
        List<ItemStack> result = new ArrayList<>();
        for(Cost cost : costs()) {
            result.addAll(cost.getItemStacks());
        }

        return result;
    }
}
