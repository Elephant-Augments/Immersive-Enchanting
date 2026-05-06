package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DatapackKeys;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import me.alfie.immersiveenchanting.api.datapack.manager.ServerDatapackManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CostRegistry {

    private final Map<Holder<Enchantment>, CostData> ENCHANTMENT_HOLDER_REGISTRY = new HashMap<>();
    private final Map<Identifier, CostData> ID_REGISTRY = new HashMap<>();

    public static final StreamCodec<RegistryFriendlyByteBuf, CostRegistry> STREAM_CODEC =
            StreamCodec.of(CostRegistry::encode, CostRegistry::decode);

    private static void encode(RegistryFriendlyByteBuf buf, CostRegistry registry) {
        Map<Identifier, CostData> map = registry.ID_REGISTRY;

        buf.writeInt(map.size());

        for (var entry : map.entrySet()) {
            buf.writeIdentifier(entry.getKey());
            CostData.STREAM_CODEC.encode(buf, entry.getValue());
        }
    }

    private static CostRegistry decode(RegistryFriendlyByteBuf buf) {
        CostRegistry registry = new CostRegistry();

        int size = buf.readInt();

        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            CostData data = CostData.STREAM_CODEC.decode(buf);

            registry.ID_REGISTRY.put(id, data);
        }

        return registry;
    }

    public static final Identifier TRANSMUTE = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "transmute");
    public static final Identifier REPLICATE = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "replicate");
    public static final Identifier ENCHANTING_FUELS = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "enchanting_fuels");

    public CostRegistry() {
    }

    public static CostRegistry client() {
        return ServerDatapackManager.get(DatapackKeys.COST);
    }

    public static CostRegistry server() {
        return ServerDatapackManager.get(DatapackKeys.COST);
    }

    public void resolveEnchantmentHolders(HolderLookup.Provider lookup) {
        HolderLookup.RegistryLookup<Enchantment> registry = lookup.lookupOrThrow(Registries.ENCHANTMENT);

        for (Identifier id : getAllEnchantmentIds()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);

            registry.get(key).ifPresentOrElse(
                    this::register,
                    () -> ImmersiveEnchanting.LOGGER.warn("Datapack contains {} but couldn't find enchantment with this id.", id)
            );
        }

        //Debug
        String side;
        if(this.equals(client())) {
            side = "client";
        } else {
            side = "server";
        }

        ImmersiveEnchanting.LOGGER.debug("Resolved enchantment holders for {}", side);
        printRegistry();
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
        CostData data = ID_REGISTRY.get(id);
        return data != null ? data : CostData.EMPTY;
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

    public Holder<Enchantment> getRandomEnchantment(RandomSource randomSource) {
        return getAllEnchantmentHolders().get(randomSource.nextInt(getAllEnchantmentHolders().size()));
    }
}
