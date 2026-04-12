package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all enchantment cost data loaded from datapacks.
 *
 * <p>This registry operates in two phases:
 * <ul>
 *     <li><b>Raw phase</b>: Stores {@link Identifier} → {@link EnchantmentCostData} loaded from JSON.</li>
 *     <li><b>Resolved phase</b>: Stores {@link Holder}&lt;{@link Enchantment}&gt; → {@link EnchantmentCostData},
 *     built after registries are available (e.g. during {@code TagsUpdatedEvent}).</li>
 * </ul>
 *
 * <p>The raw registry is the <b>source of truth</b>. The resolved registry is a derived cache
 * and must be rebuilt whenever datapacks or registries reload.</p>
 */
public class EnchantmentCostRegistry {

    /**
     * Resolved cache mapping enchantment holders to their cost data.
     *
     * <p>This map is populated after reload when registry access is available.
     * It should not be written to during datapack loading.</p>
     */
    private static final Map<Holder<Enchantment>, EnchantmentCostData> RESOLVED_ENCHANTMENT_COST_REGISTRY = new HashMap<>();

    /**
     * Raw registry mapping identifiers to cost data.
     *
     * <p>This is populated directly from datapack JSON in the reload listener.</p>
     */
    private static final Map<Identifier, EnchantmentCostData> RAW_COST_REGISTRY = new HashMap<>();

    public static final Identifier TRANSMUTE = Identifier.parse("immersiveennchanting:transmute");
    public static final Identifier REPLICATE = Identifier.parse("immersiveennchanting:replicate");
    public static final Identifier ENCHANTING_FUELS = Identifier.parse("immersiveenchanting:enchanting_fuels");

    /**
     * Registers raw cost data for the given identifier.
     *
     * <p>This is typically called during datapack reload ({@code apply()}).</p>
     *
     * @param id   The unique identifier for this cost entry.
     * @param data The associated cost data.
     */
    public static void registerId(Identifier id, EnchantmentCostData data) {
        RAW_COST_REGISTRY.put(id, data);
    }

    /**
     * Registers a resolved enchantment holder into the cache.
     *
     * <p>The corresponding identifier must already exist in the raw registry.
     * This method should only be called after registries are available
     * (e.g. during {@code TagsUpdatedEvent}).</p>
     *
     * @param enchantmentHolder The enchantment holder to resolve.
     */
    public static void registerEnchantmentHolder(Holder<Enchantment> enchantmentHolder) {
        EnchantmentCostData data = get(Identifier.parse(enchantmentHolder.getRegisteredName()));
        RESOLVED_ENCHANTMENT_COST_REGISTRY.put(enchantmentHolder, data);
    }

    /**
     * Retrieves cost data for a resolved enchantment holder.
     *
     * @param enchantmentHolder The enchantment holder.
     * @return The associated cost data, or {@code null} if not present.
     */
    public static EnchantmentCostData get(Holder<Enchantment> enchantmentHolder) {
        return RESOLVED_ENCHANTMENT_COST_REGISTRY.get(enchantmentHolder);
    }

    /**
     * Retrieves raw cost data by identifier.
     *
     * @param id The identifier of the cost entry.
     * @return The associated cost data, or {@code null} if not present.
     */
    public static EnchantmentCostData get(Identifier id) {
        return RAW_COST_REGISTRY.get(id);
    }

    /**
     * Clears both raw and resolved registries.
     *
     * <p>This should be called before reloading datapack data.</p>
     */
    public static void clear() {
        RAW_COST_REGISTRY.clear();
        RESOLVED_ENCHANTMENT_COST_REGISTRY.clear();
    }

    /**
     * Logs the contents of both raw and resolved registries for debugging purposes.
     */
    public static void printRegistry() {
        ImmersiveEnchanting.LOGGER.debug(RAW_COST_REGISTRY.toString());
        ImmersiveEnchanting.LOGGER.debug(RESOLVED_ENCHANTMENT_COST_REGISTRY.toString());
    }

    /**
     * Returns all identifiers that correspond to enchantments.
     *
     * <p>Entries within the {@code immersiveenchanting} namespace are considered
     * internal and are excluded.</p>
     *
     * @return A list of enchantment identifiers.
     */
    public static List<Identifier> getAllEnchantmentIds() {
        List<Identifier> result = new ArrayList<>();

        for (Identifier id : RAW_COST_REGISTRY.keySet()) {
            if(!id.getNamespace().equals("immersiveenchanting")) result.add(id);
        }

        return result;
    }

    /**
     * Returns all resolved enchantment holders currently cached.
     *
     * <p>This list is only valid after the resolved registry has been built.</p>
     *
     * @return A list of enchantment holders.
     */
    public static List<Holder<Enchantment>> getAllEnchantmentHolders() {
        return new ArrayList<>(RESOLVED_ENCHANTMENT_COST_REGISTRY.keySet());
    }

    /**
     * Finds the highest enchantment level defined in the cost registry.
     *
     * <p>This iterates over all raw enchantment entries and determines the maximum
     * level specified in their cost data.</p>
     *
     * @return The highest enchantment level found, or {@code 0} if none exist.
     */
    public static int getHighestLevel() {
        int highestLevel = 0;
        for (Identifier id : EnchantmentCostRegistry.getAllEnchantmentIds()) {
            int level = EnchantmentCostRegistry.get(id).levelCosts().maxLevel();
            if(level > highestLevel) highestLevel = level;
        }
        return highestLevel;
    }

}
