package me.alfie.immersiveenchanting.datapack.enchantment_cost.codec;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record ItemOrTag(Optional<Identifier> item,
                        Optional<TagKey<Item>> tag) {


    public static final Codec<ItemOrTag> CODEC = Codec.STRING.xmap(
            str -> {
                //Decode item/tag
                if(str.startsWith("#")) {
                    Identifier id = Identifier.parse(str.substring(1));
                    return new ItemOrTag(Optional.empty(),
                            Optional.of(TagKey.create(Registries.ITEM, id)));
                } else {
                    return new ItemOrTag(Optional.of(Identifier.parse(str)),
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
                    Identifier id = value.tag().get().location();
                    Identifier.STREAM_CODEC.encode(buf, id);
                } else {
                    Identifier.STREAM_CODEC.encode(buf, value.item().orElseThrow());
                }
            },
            buf -> {
                boolean isTag = buf.readBoolean();

                if(isTag) {
                    Identifier id = Identifier.STREAM_CODEC.decode(buf);
                    return new ItemOrTag(
                            Optional.empty(),
                            Optional.of(TagKey.create(Registries.ITEM, id))
                    );
                } else {
                    Identifier id = Identifier.STREAM_CODEC.decode(buf);
                    return new ItemOrTag(
                            Optional.of(id),
                            Optional.empty()
                    );
                }
            }
    );

    public static final ItemOrTag EMPTY = new ItemOrTag(Optional.of(Identifier.parse("minecraft:air")), Optional.empty());

    /**
     * Returns all items found in ItemOrTag.
     * @return
     */
    public List<Holder<Item>> getItems() {
        if (tag().isPresent()) {
            return BuiltInRegistries.ITEM.get(tag().get())
                    .map(tagSet -> tagSet.stream().toList())
                    .orElse(List.of());
        } else {
            Identifier id = item().get();
            Holder.Reference<Item> item = BuiltInRegistries.ITEM.get(id).get();
            return List.of(item);
        }
    }
}
