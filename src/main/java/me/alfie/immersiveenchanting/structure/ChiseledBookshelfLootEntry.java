package me.alfie.immersiveenchanting.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.ItemOrTag;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a simple loot entry used for probabilistic item generation.
 *
 * <p>Each entry defines an item (or tag group) and a per-slot chance of it appearing.
 */
public record ChiseledBookshelfLootEntry(ItemOrTag itemOrTag, float chancePerSlot) {

    public static final Codec<ChiseledBookshelfLootEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemOrTag.CODEC.fieldOf("item").forGetter(ChiseledBookshelfLootEntry::itemOrTag),
                    Codec.FLOAT.fieldOf("chancePerSlot").forGetter(ChiseledBookshelfLootEntry::chancePerSlot)
            ).apply(instance, ChiseledBookshelfLootEntry::new));

    /**
     * @return items in itemOrTag() as ItemStacks with a size of 1.
     */
    public List<ItemStack> getItemStacks() {
        List<ItemStack> result = new ArrayList<>(itemOrTag().getItems().size());
        for(Holder<Item> item : itemOrTag().getItems()) {
            result.add(new ItemStack(item, 1));
        }
        return result;
    }

}
