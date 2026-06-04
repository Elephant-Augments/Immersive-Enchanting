package me.alfie.immersiveenchanting.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

/**
 * A 3-slot {@link net.minecraft.world.Container} backed by the persistent
 * inventory stored on a vanilla {@link EnchantingTableBlockEntity} via the
 * {@link IEnchantingTableInventory} mixin interface.
 *
 * <p>Used by the server-side menu so that placing an item in the table writes
 * directly to the block entity, and so that closing the menu does <em>not</em>
 * lose those items — they live on the BE and survive until the block is
 * destroyed.</p>
 *
 * <p>This subclasses {@link SimpleContainer} only to inherit some of its
 * trivial behaviour and remain compatible with code that expects a
 * {@code Container}; every read/write actually overrides to forward to the
 * BE's stack list.</p>
 */
public class EnchantingTableContainer extends SimpleContainer {

    private final EnchantmentTableBlockEntity blockEntity;
    private final IEnchantingTableInventory inventoryView;

    public EnchantingTableContainer(EnchantmentTableBlockEntity blockEntity) {
        // SimpleContainer creates its own NonNullList; we overshadow every
        // method below so its internal list is never used.
        super(IEnchantingTableInventory.IMMERSIVE_INVENTORY_SIZE);
        this.blockEntity = blockEntity;
        this.inventoryView = (IEnchantingTableInventory) blockEntity;
    }

    public EnchantmentTableBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public int getContainerSize() {
        return IEnchantingTableInventory.IMMERSIVE_INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventoryView.immersive$getItems()) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventoryView.immersive$getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        NonNullList<ItemStack> items = inventoryView.immersive$getItems();
        ItemStack stack = items.get(slot);
        if (stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        ItemStack split = stack.split(amount);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        this.setChanged();
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> items = inventoryView.immersive$getItems();
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventoryView.immersive$setItem(slot, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        // Push a block update so clients receive the new contents and the
        // BlockEntityRenderer can show the floating items.
        inventoryView.immersive$setChangedAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity.isRemoved()) return false;
        return player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5
        ) <= 64.0;
    }

    @Override
    public void clearContent() {
        NonNullList<ItemStack> items = inventoryView.immersive$getItems();
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        this.setChanged();
    }
}
