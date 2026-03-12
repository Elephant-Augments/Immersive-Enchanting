package me.alfie.immersiveenchanting.networking.packet.enchantmentcostregistrysync;

import java.util.List;

/**
 * Stores a flattened enchantment cost registry for transfer between client/server.
 */
public record SerializedEnchantmentCostRegistry(
        List<String> enchantmentIds,
        List<String> jsonStrings) {}
