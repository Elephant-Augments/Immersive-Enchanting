package me.alfie.immersiveenchanting.datapack.enchantment_cost;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.*;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import me.alfie.immersiveenchanting.datapack.manager.ServerDatapackManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.Map;

public class CostDatapack extends ModDatapack<CostData, CostRegistry> {

    private final CostRegistry DATA = new CostRegistry();

    protected CostDatapack() {
        super(CostData.CODEC, DatapackKeys.COST, CostRegistry.STREAM_CODEC);
    }

    @Override
    public CostRegistry getData() {
        return DATA;
    }

    @Override
    protected void apply(Map<Identifier, CostData> identifierEnchantmentDataMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        getData().clear();

        int count = 0;
        for(Map.Entry<Identifier, CostData> entry : identifierEnchantmentDataMap.entrySet()) {
            Identifier id = remapIdentifierPath(entry.getKey());
            CostData data = entry.getValue();

            if(data.enabled()) {
                getData().register(id, data);
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

    @Override
    public void afterPull(MinecraftServer server) {
        ServerDatapackManager.get(DatapackKeys.COST).resolveEnchantmentHolders(server.registryAccess());
    }

    public static void register(AddServerReloadListenersEvent event) {
        DatapackRegistry.register(event, CostDatapack::new);
    }
}
