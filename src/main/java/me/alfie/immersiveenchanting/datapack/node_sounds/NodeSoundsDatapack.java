package me.alfie.immersiveenchanting.datapack.node_sounds;

import me.alfie.alfinolib.datapacks.DatapackKey;
import me.alfie.alfinolib.datapacks.DatapackRegistry;
import me.alfie.alfinolib.datapacks.ModDatapack;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeSoundsDatapack extends ModDatapack<NodeSoundMap, NodeSoundMap> {

    public static DatapackKey<NodeSoundMap> KEY = new DatapackKey<>(ImmersiveEnchanting.MODID, "sounds");
    private NodeSoundMap data = new NodeSoundMap(new HashMap<>());

    protected NodeSoundsDatapack(RegistryAccess registryAccess) {
        super(NodeSoundMap.CODEC, KEY, NodeSoundMap.STREAM_CODEC, registryAccess);
    }

    @Override
    protected void apply(Map<Identifier, NodeSoundMap> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Identifier key = Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "node_sounds");
        data = input.getOrDefault(key, new NodeSoundMap(new HashMap<>()));

        ImmersiveEnchanting.LOGGER.debug("Found {}", String.valueOf(data));
    }

    @Override
    public NodeSoundMap getData() {
        return data;
    }

    public static void register(AddServerReloadListenersEvent event) {
        DatapackRegistry.register(event, () -> new NodeSoundsDatapack(event.getRegistryAccess()));
    }
}
