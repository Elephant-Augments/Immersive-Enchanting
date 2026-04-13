package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * Returns all items found in ItemOrTag.
     * @return
     */
    public List<ItemStack> getItemStacks() {
        if(itemOrTag().tag().isPresent()) {
            List<Item> itemsInTag = BuiltInRegistries.ITEM.get(itemOrTag().tag().get())
                    .map(tagSet -> tagSet.stream()
                            .map(Holder::value)
                            .collect(Collectors.toList())).orElse(List.of());

            List<ItemStack> itemStacks = new ArrayList<>();
            for(Item item : itemsInTag) {
                itemStacks.add(new ItemStack(item, amount()));
            }
            return itemStacks;

        } else {
            Identifier id = itemOrTag.item().get();
            Item item = BuiltInRegistries.ITEM.get(id).get().value();
            return List.of(new ItemStack(item, amount()));
        }
    }

}
