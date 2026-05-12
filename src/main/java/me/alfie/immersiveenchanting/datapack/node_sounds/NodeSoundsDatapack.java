package me.alfie.immersiveenchanting.datapack.node_sounds;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import me.alfie.immersiveenchanting.api.datapack.DatapackRegistry;
import me.alfie.immersiveenchanting.api.datapack.ModDatapack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeSoundsDatapack extends ModDatapack<NodeSoundMap, NodeSoundMap> {

    private NodeSoundMap data = new NodeSoundMap(new HashMap<>());

    protected NodeSoundsDatapack() {
        super(NodeSoundMap.CODEC, DatapackKeys.NODE_SOUNDS, NodeSoundMap.STREAM_CODEC);
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
        DatapackRegistry.register(event, NodeSoundsDatapack::new);
    }
}
