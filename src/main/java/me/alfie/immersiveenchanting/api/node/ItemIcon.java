package me.alfie.immersiveenchanting.api.node;

import net.minecraft.world.item.ItemStack;

/**
 * A node icon rendered using an {@link ItemStack}.
 * <p>
 * The item is rendered similarly to how items appear in inventories,
 * including item models, animations, and overlays (e.g. enchant glint).
 *
 * @param stack item stack used as the visual representation
 */
public record ItemIcon(ItemStack stack) implements NodeIcon {
}
