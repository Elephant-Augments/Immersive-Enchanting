package me.alfie.immersiveenchanting.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Interface implemented (via Mixin / duck-typing) by the vanilla
 * {@link net.minecraft.world.level.block.entity.EnchantingTableBlockEntity}
 * to expose a persistent 3-slot inventory that survives menu close.
 *
 * <p>The slot layout matches the one used by Immersive Enchanting's
 * {@code EnchantingTableMenu}:</p>
 * <ul>
 *     <li>0 – Tool / item to enchant</li>
 *     <li>1 – Enchanting fuel (lapis lazuli, etc.)</li>
 *     <li>2 – Cost / catalyst item</li>
 * </ul>
 *
 * <p>Cast a vanilla {@code EnchantingTableBlockEntity} to this interface to
 * read or write the persistent stacks. The implementation lives in
 * {@code EnchantingTableBlockEntityMixin}.</p>
 */
public interface IEnchantingTableInventory {

    int IMMERSIVE_INVENTORY_SIZE = 3;

    /**
     * @return the live, mutable backing list of three item stacks. Mutating
     * elements directly should be followed by a call to
     * {@link #immersive$setChangedAndSync()} so the client is kept
     * in sync with the new contents.
     */
    NonNullList<ItemStack> immersive$getItems();

    ItemStack immersive$getItem(int slot);

    void immersive$setItem(int slot, ItemStack stack);

    /**
     * Marks the BE dirty and pushes a block update so that clients receive the
     * new stacks via {@code getUpdatePacket}/{@code getUpdateTag}, which in
     * turn drives the floating-items renderer.
     */
    void immersive$setChangedAndSync();
}
