package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.Map;

public class CostDatapack extends SimpleJsonResourceReloadListener {

    private static CostDatapack INSTANCE;
    private static final String DIRECTORY = "enchantment_costs";
    private final CostRegistry TEMP = new CostRegistry();

    protected CostDatapack(Gson gson, String directory) {
        super(gson, directory);
    }

    public static CostDatapack getInstance() {
        return INSTANCE;
    }

    public CostRegistry getBuilt() {
        return TEMP;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        TEMP.clear();

        int count = 0;
        for(Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation id = remapIdentifierPath(entry.getKey());

            CostData data = CostData.CODEC
                    .parse(JsonOps.INSTANCE, entry.getValue())
                    .resultOrPartial(error -> ImmersiveEnchanting.LOGGER.error("Failed to parse {}: {}", id, error))
                    .orElse(null);

            if(data != null && data.enabled()) {
                TEMP.register(id, data);
                count++;
            }
        }

        ImmersiveEnchanting.LOGGER.debug("Populated temp cost registry with {} entries, ready to pull.", count);

    }

    private static ResourceLocation remapIdentifierPath(ResourceLocation originalId) {
        String path = originalId.getPath();
        path = path.replace("/", ":");
        return ResourceLocation.parse(path);
    }

    public static void registerServerDatapack(AddReloadListenerEvent event) {
        CostDatapack datapack = new CostDatapack(new Gson(), DIRECTORY);
        INSTANCE = datapack;

        event.addListener(datapack);
    }


}
