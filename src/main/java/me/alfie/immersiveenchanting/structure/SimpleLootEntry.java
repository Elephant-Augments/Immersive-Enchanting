package me.alfie.immersiveenchanting.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.ItemOrTag;

/**
 * Represents a simple loot entry used for probabilistic item generation.
 *
 * <p>Each entry defines an item (or tag group) and a per-slot chance of it appearing.
 */
public record SimpleLootEntry(ItemOrTag itemOrTag, float chancePerSlot) {

    public static final Codec<SimpleLootEntry> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ItemOrTag.CODEC.fieldOf("item").forGetter(SimpleLootEntry::itemOrTag),
                    Codec.FLOAT.fieldOf("chancePerSlot").forGetter(SimpleLootEntry::chancePerSlot)
            ).apply(instance, SimpleLootEntry::new));

}
