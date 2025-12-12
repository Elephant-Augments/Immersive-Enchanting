package me.alfie.immersiveenchanting.networking;

import java.util.List;

/**
 * Stores a flattened enchantment cost registry for transfer between client/server.
 * @param enchantmentNamespaces
 * @param levels
 * @param itemIds
 * @param amounts
 */
public record SerializedEnchantmentCostRegistry(
        List<String> enchantmentNamespaces,
        List<String> levels,
        List<String> itemIds,
        List<Integer> amounts,
        String lapisCostItemId,
        int lapisCostAmount) {}
