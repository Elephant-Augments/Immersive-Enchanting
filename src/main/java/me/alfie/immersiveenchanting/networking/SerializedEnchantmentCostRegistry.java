package me.alfie.immersiveenchanting.networking;

import java.util.List;

/**
 * Stores a flattened enchantment cost registry for transfer between client/server.
 */
public record SerializedEnchantmentCostRegistry(
        List<String> enchantmentIds,
        List<String> jsonStrings) {}
