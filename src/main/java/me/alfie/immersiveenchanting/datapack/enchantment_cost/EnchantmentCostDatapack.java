package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.Map;

public class EnchantmentCostDatapack extends SimpleJsonResourceReloadListener<EnchantmentCostData> {

    private static final String DIRECTORY = "enchantment_costs";

    protected EnchantmentCostDatapack(Codec<EnchantmentCostData> codec, FileToIdConverter lister) {
        super(codec, lister);
    }

    @Override
    protected void apply(Map<Identifier, EnchantmentCostData> identifierEnchantmentDataMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        EnchantmentCostRegistry.clear();

        int count = 0;
        for(Map.Entry<Identifier, EnchantmentCostData> entry : identifierEnchantmentDataMap.entrySet()) {
            Identifier id = getId(entry.getKey());
            EnchantmentCostData data = entry.getValue();

            if(data.enabled()) {
                EnchantmentCostRegistry.registerId(id, data);
                count++;
            }
        }

        ImmersiveEnchanting.LOGGER.debug("Populated enchantment cost registry with {} entries.", count);
    }

    private Identifier getId(Identifier originalId) {
        String path = originalId.getPath();
        path = path.replace("/", ":");
        return Identifier.parse(path);
    }

    public static void registerServerDatapack(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY),
                new EnchantmentCostDatapack(EnchantmentCostData.CODEC, FileToIdConverter.json(DIRECTORY)));
    }

    public static void registerClientDatapack(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY),
                new EnchantmentCostDatapack(EnchantmentCostData.CODEC, FileToIdConverter.json(DIRECTORY)));
    }

    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        for (Identifier id : EnchantmentCostRegistry.getAllEnchantmentIds()) {
            Holder<Enchantment> enchantmentHolder = event.getLookupProvider()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .get(ResourceKey.create(Registries.ENCHANTMENT, id))
                    .orElseThrow();

            EnchantmentCostRegistry.registerEnchantmentHolder(enchantmentHolder);
        }

        ImmersiveEnchanting.LOGGER.debug("Tags updated.");
    }
}
