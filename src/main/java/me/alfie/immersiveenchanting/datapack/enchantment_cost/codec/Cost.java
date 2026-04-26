package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

    public static final StreamCodec<RegistryFriendlyByteBuf, Cost> STREAM_CODEC = StreamCodec.composite(
            ItemOrTag.STREAM_CODEC, Cost::itemOrTag,
            ByteBufCodecs.VAR_INT, Cost::amount,
            ByteBufCodecs.VAR_INT, Cost::xpLevels,
            Cost::new);

    /**
     * Returns all items found in ItemOrTag with this amount.
     * @return
     */
    public List<ItemStack> getItemStacks() {
        return itemOrTag().getItemStacks(amount());
    }

}
