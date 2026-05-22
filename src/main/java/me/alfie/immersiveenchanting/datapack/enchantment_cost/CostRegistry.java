package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import me.alfie.immersiveenchanting.api.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.api.datapack.manager.ServerDatapackManager;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CostRegistry {

    public static final StreamCodec<RegistryFriendlyByteBuf, CostRegistry> STREAM_CODEC =
            StreamCodec.of(CostRegistry::encode, CostRegistry::decode);
    public static final ResourceLocation TRANSMUTE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "transmute");
    public static final ResourceLocation REPLICATE = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "replicate");
    public static final ResourceLocation ENCHANTING_FUELS = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "enchanting_fuels");
    private final Map<Holder<Enchantment>, CostData> ENCHANTMENT_HOLDER_REGISTRY = new HashMap<>();
    private final Map<ResourceLocation, CostData> ID_REGISTRY = new HashMap<>();
    public CostRegistry() {
    }

    private static void encode(RegistryFriendlyByteBuf buf, CostRegistry registry) {
        Map<ResourceLocation, CostData> map = registry.ID_REGISTRY;

        buf.writeInt(map.size());

        for (var entry : map.entrySet()) {
            buf.writeResourceLocation(entry.getKey());
            CostData.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    private static CostRegistry decode(RegistryFriendlyByteBuf buf) {
        CostRegistry registry = new CostRegistry();

        int size = buf.readInt();

        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            CostData data = CostData.STREAM_CODEC.decode(buf);

            registry.ID_REGISTRY.put(id, data);
        }

        return registry;
    }

    public static CostRegistry client() {
        return ClientDatapackManager.get(DatapackKeys.COST);
    }

    public static CostRegistry server() {
        return ServerDatapackManager.get(DatapackKeys.COST);
    }

    /**
     * Populates the holder-keyed registry from the ID-keyed registry using the provided
     * lookup. Must be called after the ID registry is fully populated (e.g. on server start
     * or datapack reload) so that holder lookups work correctly.
     * Logs a warning for any ID that cannot be resolved to a registered enchantment.
     */
    public void resolveEnchantmentHolders(HolderLookup.Provider lookup) {
        HolderLookup.RegistryLookup<Enchantment> registry = lookup.lookupOrThrow(Registries.ENCHANTMENT);

        for (ResourceLocation id : getAllEnchantmentIds()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);

            registry.get(key).ifPresentOrElse(
                    this::register,
                    () -> ImmersiveEnchanting.LOGGER.warn("Datapack contains {} but couldn't find enchantment with this id.", id)
            );
        }
    }

    /**
     * Returns all IDs in the ID registry that are not in this mod's namespace.
     * Mod-internal entries (transmute, replicate, enchanting_fuels) are intentionally excluded.
     */
    public List<ResourceLocation> getAllEnchantmentIds() {
        List<ResourceLocation> result = new ArrayList<>();

        for (ResourceLocation id : ID_REGISTRY.keySet()) {
            if (!id.getNamespace().equals(ImmersiveEnchanting.MODID)) result.add(id);
        }

        return result;
    }

    /**
     * Registers cost data by enchantment holder. Requires the same ID to already be present
     * in the ID registry (i.e. {@link #register(ResourceLocation, CostData)} must have been called first).
     */
    public void register(Holder<Enchantment> enchantmentHolder) {
        ResourceLocation id = ResourceLocation.parse(enchantmentHolder.getRegisteredName());
        CostData data = get(id);

        if (data == null) {
            throw new IllegalStateException("No CostData registered for id '" + id +
                    "'. You must call register(id, data) before registering the enchantment.");
        }

        ENCHANTMENT_HOLDER_REGISTRY.put(enchantmentHolder, data);
    }

    public CostData get(ResourceLocation id) {
        CostData data = ID_REGISTRY.get(id);
        return data != null ? data : CostData.EMPTY;
    }

    /**
     * Registers raw cost data by ID. Used during datapack loading before holders are resolved.
     */
    public void register(ResourceLocation id, CostData data) {
        ID_REGISTRY.put(id, data);
    }

    public boolean isRegistered(Holder<Enchantment> enchantmentHolder) {
        return ENCHANTMENT_HOLDER_REGISTRY.containsKey(enchantmentHolder);
    }

    public boolean isRegistered(ResourceLocation id) {
        return ID_REGISTRY.containsKey(id);
    }

    public void clear() {
        ID_REGISTRY.clear();
        ENCHANTMENT_HOLDER_REGISTRY.clear();
    }

    public void printRegistry() {
        ImmersiveEnchanting.LOGGER.debug("ID Registry: {}", ID_REGISTRY);
        ImmersiveEnchanting.LOGGER.debug("Holder registry (resolved) {}: ", ENCHANTMENT_HOLDER_REGISTRY);
    }

    /**
     * Returns the highest enchantment level defined across all registered enchantments.
     * Used to size the canvas so the deepest branch is not clipped.
     */
    public int getHighestLevel() {
        int highestLevel = 0;
        for (ResourceLocation id : getAllEnchantmentIds()) {
            int level = get(id).levelCosts().maxLevel();
            if (level > highestLevel) highestLevel = level;
        }
        return highestLevel;
    }

    public Holder<Enchantment> getRandomEnchantment(RandomSource randomSource) {
        return getAllEnabledEnchantmentHolders().get(randomSource.nextInt(getAllEnabledEnchantmentHolders().size()));
    }

    /**
     * Returns only the enabled enchantments in the registry. Excludes disabled ones.
     */
    public List<Holder<Enchantment>> getAllEnabledEnchantmentHolders() {
        List<Holder<Enchantment>> result = getAllEnchantmentHolders();
        result.removeIf(holder -> !get(holder).enabled());
        return result;
    }

    /**
     * Returns all enchantment holders in the registry, including disabled ones.
     */
    public List<Holder<Enchantment>> getAllEnchantmentHolders() {
        return new ArrayList<>(ENCHANTMENT_HOLDER_REGISTRY.keySet());
    }

    public CostData get(Holder<Enchantment> enchantmentHolder) {
        return ENCHANTMENT_HOLDER_REGISTRY.get(enchantmentHolder);
    }
}
