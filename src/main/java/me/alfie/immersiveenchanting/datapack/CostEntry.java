package me.alfie.immersiveenchanting.datapack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record CostEntry(String item, String nbt, int amount, int xpLevels) implements CostDefinition {

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

    @Override
    public CostDefinition resolveTags() {
        if (!CostHelper.isItemTag(item)) {
            return this;
        }

        String itemTag = item;

        List<CostDefinition> children =
                CostHelper.getItemsInItemTag(
                                CostHelper.getItemTag(itemTag)
                        ).stream()
                        .map(item -> (CostDefinition) new CostEntry(
                                item.toString(),
                                "",
                                amount,
                                xpLevels
                        ))
                        .toList();

        return new CostGroup(children, GroupType.ANY_OF, itemTag);
    }
}
