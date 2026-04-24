package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.Map;

public class CostDatapack extends SimpleJsonResourceReloadListener<CostData> {

    private static CostDatapack INSTANCE;
    private static final String DIRECTORY = "enchantment_costs";
    private final CostRegistry TEMP = new CostRegistry();

    protected CostDatapack(Codec<CostData> codec, FileToIdConverter lister) {
        super(codec, lister);
    }

    public static CostDatapack getInstance() {
        return INSTANCE;
    }

    public CostRegistry getBuilt() {
        return TEMP;
    }

    @Override
    protected void apply(Map<Identifier, CostData> identifierEnchantmentDataMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        TEMP.clear();

        int count = 0;
        for(Map.Entry<Identifier, CostData> entry : identifierEnchantmentDataMap.entrySet()) {
            Identifier id = remapIdentifierPath(entry.getKey());
            CostData data = entry.getValue();

            if(data.enabled()) {
                TEMP.register(id, data);
                count++;
            }
        }

        ImmersiveEnchanting.LOGGER.debug("Populated temp cost registry with {} entries, ready to pull.", count);
    }

    private static Identifier remapIdentifierPath(Identifier originalId) {
        String path = originalId.getPath();
        path = path.replace("/", ":");
        return Identifier.parse(path);
    }

    public static void registerServerDatapack(AddServerReloadListenersEvent event) {
        CostDatapack datapack = new CostDatapack(CostData.CODEC, FileToIdConverter.json(DIRECTORY));
        INSTANCE = datapack;

        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY), datapack);
    }
}
