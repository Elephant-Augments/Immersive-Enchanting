package me.alfie.immersiveenchanting.datapack.legacy;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record LevelCost(String item, int amount, @SerializedName("xp_levels") int xpLevels) {

    public LevelCost(String item, int amount) {
        this(item, amount, 0);
    }

    //Special tag to prevent the enchantment from appearing in the enchanting table/spawning as ancient books.
    public static final String DO_NOT_INCLUDE = "DO_NOT_INCLUDE";

    public ItemStack asItemStack() {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(this.item);
        Item item = BuiltInRegistries.ITEM.get(resourceLocation);

        if (item == Items.AIR || this.amount <= 0) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, this.amount());
    }
}
