package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record ItemOrTag(Optional<ResourceLocation> item,
                        Optional<TagKey<Item>> tag) {


    public static final Codec<ItemOrTag> CODEC = Codec.STRING.xmap(
            str -> {
                //Decode item/tag
                if(str.startsWith("#")) {
                    ResourceLocation id = ResourceLocation.parse(str.substring(1));
                    return new ItemOrTag(Optional.empty(),
                            Optional.of(TagKey.create(Registries.ITEM, id)));
                } else {
                    return new ItemOrTag(Optional.of(ResourceLocation.parse(str)),
                            Optional.empty());
                }
            },
            //Encode item/tag
            itemOrTag -> {
                return itemOrTag.tag
                        .map(itemTagKey -> "#" + itemTagKey.location())
                        .orElseGet(() -> itemOrTag.item.orElseThrow().toString());
            }
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemOrTag> STREAM_CODEC = StreamCodec.of(
            (buf, value) -> {
                boolean isTag = value.tag().isPresent();
                buf.writeBoolean(isTag);

                if(isTag) {
                    ResourceLocation id = value.tag().get().location();
                    ResourceLocation.STREAM_CODEC.encode(buf, id);
                } else {
                    ResourceLocation.STREAM_CODEC.encode(buf, value.item().orElseThrow());
                }
            },
            buf -> {
                boolean isTag = buf.readBoolean();

                if(isTag) {
                    ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
                    return new ItemOrTag(
                            Optional.empty(),
                            Optional.of(TagKey.create(Registries.ITEM, id))
                    );
                } else {
                    ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
                    return new ItemOrTag(
                            Optional.of(id),
                            Optional.empty()
                    );
                }
            }
    );

    /**
     * Returns all items found in ItemOrTag.
     * @return
     */
    public List<ItemStack> getItemStacks(int amount) {
        if(tag().isPresent()) {
            List<Item> itemsInTag = BuiltInRegistries.ITEM.getTag(tag().get())
                    .map(tagSet -> tagSet.stream()
                            .map(Holder::value)
                            .collect(Collectors.toList())).orElse(List.of());

            List<ItemStack> itemStacks = new ArrayList<>();
            for(Item item : itemsInTag) {
                itemStacks.add(new ItemStack(item, amount));
            }
            return itemStacks;

        } else {
            ResourceLocation id = item().get();
            Item item = BuiltInRegistries.ITEM.get(id);
            return List.of(new ItemStack(item, amount));
        }
    }
}
