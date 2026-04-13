package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.manager.ClientCostManager;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.manager.ServerCostManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CostRegistry {

    private final Map<Holder<Enchantment>, CostData> ENCHANTMENT_HOLDER_REGISTRY = new HashMap<>();
    private final Map<Identifier, CostData> ID_REGISTRY = new HashMap<>();

    public static final Identifier TRANSMUTE = Identifier.parse("immersiveennchanting:transmute");
    public static final Identifier REPLICATE = Identifier.parse("immersiveennchanting:replicate");
    public static final Identifier ENCHANTING_FUELS = Identifier.parse("immersiveenchanting:enchanting_fuels");

    public CostRegistry() {

    }

    public CostRegistry(Map<Identifier, CostData> idRegistry) {
        ID_REGISTRY.putAll(idRegistry);
    }

    public static CostRegistry client() {
        return ClientCostManager.registry();
    }

    public static CostRegistry server() {
        return ServerCostManager.registry();
    }

    public static void resolveEnchantmentHolders(CostRegistry registry, HolderLookup.Provider lookup) {
        for (Identifier id : registry.getAllEnchantmentIds()) {
            Holder<Enchantment> enchantmentHolder = lookup.lookupOrThrow(Registries.ENCHANTMENT)
                    .get(ResourceKey.create(Registries.ENCHANTMENT, id))
                    .orElseThrow();

            registry.register(enchantmentHolder);
        }

        //Debug
        String side;
        if(registry.equals(client())) {
            side = "client";
        } else {
            side = "server";
        }

        ImmersiveEnchanting.LOGGER.debug("Resolved enchantment holders for {}", side);
        registry.printRegistry();
    }

    public void register(Identifier id, CostData data) {
        ID_REGISTRY.put(id, data);
    }

    public void register(Holder<Enchantment> enchantmentHolder) {
        Identifier id = Identifier.parse(enchantmentHolder.getRegisteredName());
        CostData data = get(id);

        if(data == null) {
            throw new IllegalStateException("No CostData registered for id '" + id +
                    "'. You must call register(id, data) before registering the enchantment.");
        }

        ENCHANTMENT_HOLDER_REGISTRY.put(enchantmentHolder, data);
    }

    public CostData get(Holder<Enchantment> enchantmentHolder) {
        return ENCHANTMENT_HOLDER_REGISTRY.get(enchantmentHolder);
    }

    public CostData get(Identifier id) {
        return ID_REGISTRY.get(id);
    }

    public void clear() {
        ID_REGISTRY.clear();
        ENCHANTMENT_HOLDER_REGISTRY.clear();
    }

    public void printRegistry() {
        ImmersiveEnchanting.LOGGER.debug(ID_REGISTRY.toString());
        ImmersiveEnchanting.LOGGER.debug(ENCHANTMENT_HOLDER_REGISTRY.toString());
    }

    public List<Identifier> getAllEnchantmentIds() {
        List<Identifier> result = new ArrayList<>();

        for (Identifier id : ID_REGISTRY.keySet()) {
            if(!id.getNamespace().equals(ImmersiveEnchanting.MODID)) result.add(id);
        }

        return result;
    }

    public List<Holder<Enchantment>> getAllEnchantmentHolders() {
        return new ArrayList<>(ENCHANTMENT_HOLDER_REGISTRY.keySet());
    }

    public int getHighestLevel() {
        int highestLevel = 0;
        for (Identifier id : getAllEnchantmentIds()) {
            int level = get(id).levelCosts().maxLevel();
            if(level > highestLevel) highestLevel = level;
        }
        return highestLevel;
    }

    public Map<Identifier, CostData> snapshotIdRegistry() {
        return Map.copyOf(ID_REGISTRY);
    }
}
