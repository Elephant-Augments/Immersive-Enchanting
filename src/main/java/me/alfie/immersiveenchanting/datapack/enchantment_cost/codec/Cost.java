package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record Cost(ItemOrTagStack itemStackHolder, int xpLevels) {

    public static final Codec<Cost> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemOrTagStack.CODEC.fieldOf("item_stack").forGetter(Cost::itemStackHolder),
                    Codec.INT.optionalFieldOf("xp_levels", 0).forGetter(Cost::xpLevels)
            ).apply(instance, Cost::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Cost> STREAM_CODEC = StreamCodec.composite(
            ItemOrTagStack.STREAM_CODEC, Cost::itemStackHolder,
            ByteBufCodecs.VAR_INT, Cost::xpLevels,
            Cost::new);

    public static final Cost EMPTY = new Cost(ItemOrTagStack.EMPTY, 0);

    /**
     * Convenience method to getItemStacks() on itemStackHolder()
     */
    public List<ItemStack> getItemStacks() {
        return itemStackHolder().getItemStacks();
    }
}
