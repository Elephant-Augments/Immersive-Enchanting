package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.networking.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
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

    public static final StreamCodec<CostHolder> STREAM_CODEC = new StreamCodec<CostHolder>() {
        @Override
        public void encode(FriendlyByteBuf buf, CostHolder value) {
            List<Cost> costs = value.costs();

            buf.writeInt(costs.size());
            for (Cost cost : costs) {
                Cost.STREAM_CODEC.encode(buf, cost);
            }
        }

        @Override
        public CostHolder decode(FriendlyByteBuf buf) {
            int size = buf.readInt();

            List<Cost> costs = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                costs.add(Cost.STREAM_CODEC.decode(buf));
            }

            return new CostHolder(costs);
        }
    };

    public List<ItemStack> getAllItemStacks() {
        List<ItemStack> result = new ArrayList<>();
        for(Cost cost : costs()) {
            result.addAll(cost.getItemStacks());
        }

        return result;
    }
}
