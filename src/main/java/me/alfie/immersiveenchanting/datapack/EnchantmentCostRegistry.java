package me.alfie.immersiveenchanting.datapack;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentCostRegistry {
    private static final Map<Identifier, EnchantmentCostData> ENCHANTMENT_COSTS = new HashMap<>();

    public static final Identifier TRANSMUTE = Identifier.parse("immersiveennchanting:transmute");
    public static final Identifier REPLICATE = Identifier.parse("immersiveennchanting:replicate");
    public static final Identifier ENCHANTING_FUELS = Identifier.parse("immersiveenchanting:enchanting_fuels");

    /**
     * Register a new enchantment cost.
     * @param id
     * @param data
     */
    public static void register(Identifier id, EnchantmentCostData data) {
        ENCHANTMENT_COSTS.put(id, data);
    }

    public static EnchantmentCostData get(Identifier id) {
        return ENCHANTMENT_COSTS.get(id);
    }

    public static List<EnchantmentCost> getCostsFor(Identifier id, int level) {
        return get(id).levelCosts().getLevel(level).costs();
    }

    public static void clear() {
        ENCHANTMENT_COSTS.clear();
    }

    public static void printRegistry() {
        System.out.println(ENCHANTMENT_COSTS);
    }

    /**
     * Get all enchantments in the registry - excludes the immersiveenchanting namespace.
     * @return
     */
    public static List<Identifier> getAllEnchantmentIds() {
        List<Identifier> result = new ArrayList<>();

        for (Identifier id : ENCHANTMENT_COSTS.keySet()) {
            if(!id.getNamespace().equals("immersiveenchanting")) result.add(id);
        }

        return result;
    }

}
