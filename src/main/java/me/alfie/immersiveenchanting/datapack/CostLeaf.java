package me.alfie.immersiveenchanting.datapack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record CostLeaf(String item, String nbt, int amount, int xpLevels) implements CostNode {

    public Item asItem() {
        //Parse item id
        ResourceLocation resourceLocation = ResourceLocation.tryParse(this.item);
        Item item = BuiltInRegistries.ITEM.get(resourceLocation);

        return item;
    }

    public ItemStack asItemStack() {
        //Parse item id
        ResourceLocation resourceLocation = ResourceLocation.tryParse(this.item);
        Item item = BuiltInRegistries.ITEM.get(resourceLocation);

        //Todo add nbt support
        ItemStack itemStack = new ItemStack(item, this.amount);
        return itemStack;
    }


}
