package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ItemOrTagStack(ItemOrTag itemOrTag,
                             DataComponentPatch components,
                             int amount) {

    public static final Codec<ItemOrTagStack> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemOrTag.CODEC.fieldOf("item_or_tag_id").forGetter(ItemOrTagStack::itemOrTag),
                    DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemOrTagStack::components),
                    Codec.INT.fieldOf("count").forGetter(ItemOrTagStack::amount)
            ).apply(instance, ItemOrTagStack::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOrTagStack> STREAM_CODEC = StreamCodec.composite(
            ItemOrTag.STREAM_CODEC, ItemOrTagStack::itemOrTag,
            DataComponentPatch.STREAM_CODEC, ItemOrTagStack::components,
            ByteBufCodecs.VAR_INT, ItemOrTagStack::amount,
            ItemOrTagStack::new
    );

    public static final ItemOrTagStack EMPTY = new ItemOrTagStack(ItemOrTag.EMPTY, DataComponentPatch.EMPTY, 0);

    public List<ItemStack> getItemStacks() {
        List<ItemStack> result = new ArrayList<>(itemOrTag().getItems().size());
        for (Holder<Item> item : itemOrTag().getItems()) {
            result.add(build(item, amount(), components()));
        }
        return result;
    }

    private static ItemStack build(Holder<Item> item, int amount, DataComponentPatch components) {
        return new ItemStack(item, amount, components);
    }

}
