package me.alfie.immersiveenchanting.datapack.cost;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Represents an item stack
 * @param item The item resource location
 * @param nbt Additional NBT data
 * @param amount Amount of the item
 * @param xpLevels Number of XP levels required
 */
public record CostEntry(String item, String nbt, int amount, int xpLevels, @Nullable CostItemTag costItemTag) implements CostDefinition {

    public static final CostEntry EMPTY = new CostEntry("minecraft:air", "", 0, 0);

    public CostEntry(String item, String nbt, int amount, int xpLevels) {
        this(item, nbt, amount,xpLevels, null);
    }

    public Item asItem() {
        //Parse item id
        ResourceLocation resourceLocation = ResourceLocation.tryParse(this.item);
        Item item = BuiltInRegistries.ITEM.get(resourceLocation);

        return item;
    }

    public ItemStack asItemStack() {
        Item item = asItem();

        //Todo add nbt support
        ItemStack itemStack = new ItemStack(item, this.amount);
        return itemStack;
    }

    public Optional<CostItemTag> getCostItemTag() {
        return Optional.ofNullable(costItemTag());
    }
}
