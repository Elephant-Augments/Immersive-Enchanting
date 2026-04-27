package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.networking.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record Cost(ItemOrTag itemOrTag, int amount, int xpLevels) {

    public static final Codec<Cost> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemOrTag.CODEC.fieldOf("item").forGetter(Cost::itemOrTag),
                    Codec.INT.fieldOf("amount").forGetter(Cost::amount),
                    Codec.INT.optionalFieldOf("xp_levels", 0).forGetter(Cost::xpLevels)
            ).apply(instance, Cost::new)
    );

    public static final StreamCodec<Cost> STREAM_CODEC = new StreamCodec<Cost>() {
        @Override
        public void encode(FriendlyByteBuf buf, Cost value) {
            ItemOrTag.STREAM_CODEC.encode(buf, value.itemOrTag());
            buf.writeInt(value.amount());
            buf.writeInt(value.xpLevels());
        }

        @Override
        public Cost decode(FriendlyByteBuf buf) {
            ItemOrTag itemOrTag = ItemOrTag.STREAM_CODEC.decode(buf);
            int amount = buf.readInt();
            int xpLevels = buf.readInt();

            return new Cost(itemOrTag, amount, xpLevels);
        }
    };

    /**
     * Returns all items found in ItemOrTag with this amount.
     * @return
     */
    public List<ItemStack> getItemStacks() {
        return itemOrTag().getItemStacks(amount());
    }

}
