package me.alfie.immersiveenchanting.datapack;

import me.alfie.immersiveenchanting.datapack.legacy.LegacyEnchantmentCost;
import me.alfie.immersiveenchanting.datapack.legacy.LevelCost;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashMap;
import java.util.Map;

//This class will be instancable, so there will be a server-side one and a client-side one.

public class EnchantmentCostRegistry {
    private static EnchantmentCostRegistry serverEnchantmentCostRegistry; //Server-side access only
    private static EnchantmentCostRegistry clientEnchantmentCostRegistry; //Updated by server, safe to use on client

    public static EnchantmentCostRegistry getClientRegistry() {
        return clientEnchantmentCostRegistry;
    }

    public static EnchantmentCostRegistry getServerRegistry() {
        return serverEnchantmentCostRegistry;
    }

    public static void setClientRegistry(EnchantmentCostRegistry enchantmentCostRegistry) {
        clientEnchantmentCostRegistry = enchantmentCostRegistry;
    }

    public static void setServerRegistry(EnchantmentCostRegistry enchantmentCostRegistry) {
        serverEnchantmentCostRegistry = enchantmentCostRegistry;
    }

    // Maps the enchantment ResourceLocation (e.g., minecraft:efficiency) to its cost data
    private final Map<ResourceKey<Enchantment>, LegacyEnchantmentCost> COST_REGISTRY = new HashMap<>();
    private ItemStack lapisCost;


    public static final LegacyEnchantmentCost EMPTY = new LegacyEnchantmentCost();

    /**
     * Helper method to get enchantment cost from COST_REGISTRY from its resource location.
     * @param enchantment
     * @return
     */
    public LegacyEnchantmentCost getEnchantmentCost(ResourceKey<Enchantment> enchantment) {
        return this.COST_REGISTRY.getOrDefault(enchantment, EMPTY);
    }

    /**
     * Helper method to get a specific level cost for an enchantment using its resource location.
     * @param enchantment
     * @param level
     * @return
     */
    public LevelCost getLevelCost(ResourceKey<Enchantment> enchantment, int level) {
        LegacyEnchantmentCost data = this.COST_REGISTRY.get(enchantment);
        if (data == null) return null;
        return data.getLevel(level);
    }

    /**
     * Clear the cost registry
     */
    public void clear() {
        this.COST_REGISTRY.clear();
    }

    /**
     * Returns the integer of the highest level within the COST_REGISTRY.
     * @return
     */
    public int getHighestEnchantmentLevel() {
        return this.COST_REGISTRY.values().stream()
                .mapToInt(LegacyEnchantmentCost::getHighestLevel)
                .max()
                .orElse(0); // return 0 if there are no enchantments
    }

    /**
     * Returns the cost registry map <ResourceLocation, LegacyEnchantmentCost>
     * @return
     */
    public Map<ResourceKey<Enchantment>, LegacyEnchantmentCost> getCostRegistry() {
        return this.COST_REGISTRY;
    }

    public ItemStack getLapisCost() {
        return lapisCost;
    }

    public void setLapisCost(ItemStack lapisCost) {
        this.lapisCost = lapisCost;
    }
}
