package me.alfie.immersiveenchanting.datapack;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.Map;

public class ModDatapack extends SimpleJsonResourceReloadListener<EnchantmentCostData> {

    private static final String DIRECTORY = "enchantment_costs";

    protected ModDatapack(Codec<EnchantmentCostData> codec, FileToIdConverter lister) {
        super(codec, lister);
    }

    @Override
    protected void apply(Map<Identifier, EnchantmentCostData> identifierEnchantmentDataMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        EnchantmentCostRegistry.clear();

        for(Map.Entry<Identifier, EnchantmentCostData> entry : identifierEnchantmentDataMap.entrySet()) {
            Identifier id = getEnchantmentId(entry.getKey());
            EnchantmentCostData data = entry.getValue();

            if(data.enabled()) {
                EnchantmentCostRegistry.register(id, data);
            }
        }
    }

    private Identifier getEnchantmentId(Identifier originalId) {
        String path = originalId.getPath();
        path = path.replace("/", ":");
        return Identifier.parse(path);
    }

    public static void registerServerDatapack(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY),
                new ModDatapack(EnchantmentCostData.CODEC, FileToIdConverter.json(DIRECTORY)));
    }

    public static void registerClientDatapack(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY),
                new ModDatapack(EnchantmentCostData.CODEC, FileToIdConverter.json(DIRECTORY)));
    }
}
