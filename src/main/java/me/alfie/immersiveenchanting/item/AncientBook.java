package me.alfie.immersiveenchanting.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

public class AncientBook extends Item {

    public AncientBook(Properties properties) {
        super(properties
                .stacksTo(16)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        return true;
    }


}
