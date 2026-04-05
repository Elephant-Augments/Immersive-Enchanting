package me.alfie.immersiveenchanting.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record EnchantmentCost(ItemOrTag itemOrTag, int amount, int xpLevels) {

    public static final Codec<EnchantmentCost> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemOrTag.CODEC.fieldOf("item").forGetter(EnchantmentCost::itemOrTag),
                    Codec.INT.fieldOf("amount").forGetter(EnchantmentCost::amount),
                    Codec.INT.optionalFieldOf("xp_levels", 0).forGetter(EnchantmentCost::xpLevels)
            ).apply(instance, EnchantmentCost::new)
    );

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
