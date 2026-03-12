package me.alfie.immersiveenchanting.datacomponent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class ReplicatedNBT {
    public static final String NBT_TAG = "replicated";

    public static void setReplicated(ItemStack stack, boolean value) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(NBT_TAG, value);
    }

    public static boolean isReplicated(ItemStack stack) {
        if(stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if(tag.contains(NBT_TAG)) {
                return tag.getBoolean(NBT_TAG);
            }
        }
        return false;
    }
}
