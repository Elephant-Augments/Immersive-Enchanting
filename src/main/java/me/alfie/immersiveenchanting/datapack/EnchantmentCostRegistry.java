package me.alfie.immersiveenchanting.datapack;

import me.alfie.immersiveenchanting.datapack.cost.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    //Maps the enchantment ResourceLocation (e.g., minecraft:efficiency) to its cost data
    private final Map<ResourceKey<Enchantment>, EnchantmentCost> COST_REGISTRY = new HashMap<>();
    public static final EnchantmentCost EMPTY = new EnchantmentCost(new HashMap<>());

    /**
     * Helper method to get enchantment cost from COST_REGISTRY from its resource location.
     * @param enchantment
     * @return
     */
    public EnchantmentCost getEnchantmentCost(ResourceKey<Enchantment> enchantment) {
        return this.COST_REGISTRY.getOrDefault(enchantment, EMPTY);
    }

    /**
     * Check if a list of items is a valid cost.
     * @param node
     * @param items
     * @param playerXp
     * @return
     */
    public static boolean isCostValid(CostDefinition node, List<ItemStack> items, int playerXp) {
        if(node instanceof CostEntry leaf) {
            boolean hasItem = false;

            //Check item
            ItemStack leafStack = leaf.asItemStack();
            for(ItemStack stack : items) {
                if(stack.is(leafStack.getItem())) {
                    //Check amount
                    if (stack.getCount() >= leafStack.getCount()) {
                        hasItem = true;
                        break;
                    }
                }
            }

            //Check XP
            boolean hasXp = playerXp >= leaf.xpLevels();
            return hasItem && hasXp;

        } else if(node instanceof CostGroup composite) {
            if(composite.type() == GroupType.ANY_OF) {
                //Any child is enough
                for(CostDefinition child : composite.children()) {
                    if(isCostValid(child, items, playerXp)) return true;
                }
                return false;
            } else {
                //All children must be valid
                for(CostDefinition child : composite.children()) {
                    if(!isCostValid(child, items, playerXp)) return false;
                }
                return true;
            }
        }

        //Never reached
        return false;
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
                .mapToInt(EnchantmentCost::getHighestLevel)
                .max()
                .orElse(0); // return 0 if there are no enchantments
    }

    /**
     * Returns the cost registry map <ResourceLocation, LegacyEnchantmentCost>
     * @return
     */
    public Map<ResourceKey<Enchantment>, EnchantmentCost> getCostRegistry() {
        return this.COST_REGISTRY;
    }

    /**
     * Returns a list of all enchantment IDs that are disabled.
     * @return
     */
    public List<String> getDisabledEnchantments() {
        List<String> disabledEnchantments = new ArrayList<>();
        for(Map.Entry<ResourceKey<Enchantment>, EnchantmentCost> entry : getCostRegistry().entrySet()) {
            EnchantmentCost cost = entry.getValue();
            if(!cost.enabled) {
                String enchantmentName = entry.getKey().location().toString();
                disabledEnchantments.add(enchantmentName);
            }
        }
        return disabledEnchantments;
    }

    /**
     * Returns a list of all enchantment IDs that are enabled.
     * @return
     */
    public List<String> getEnabledEnchantments() {
        List<String> enabledEnchantments = new ArrayList<>();
        for(Map.Entry<ResourceKey<Enchantment>, EnchantmentCost> entry : getCostRegistry().entrySet()) {
            EnchantmentCost cost = entry.getValue();
            if(cost.enabled) {
                String enchantmentName = entry.getKey().location().toString();
                enabledEnchantments.add(enchantmentName);
            }
        }
        return enabledEnchantments;
    }
}
