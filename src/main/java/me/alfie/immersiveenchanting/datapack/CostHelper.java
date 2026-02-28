package me.alfie.immersiveenchanting.datapack;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CostHelper {
    public static List<Item> getItemsInItemTag(TagKey<Item> itemTag) {
        return BuiltInRegistries.ITEM.getTag(itemTag)
                .map(tagSet -> tagSet.stream()
                        .map(Holder::value) // <-- convert Holder<Item> -> Item
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Automatically removes the # prefix
     * @param itemTag
     * @return
     */
    public static TagKey<Item> getItemTag(String itemTag) {
        itemTag = itemTag.replaceFirst("#", "");
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.parse(itemTag));
        return tag;
    }

    public static boolean isItemTag(String id) {
        return id.startsWith("#");
    }
}
