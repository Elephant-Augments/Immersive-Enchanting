package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.google.gson.JsonElement;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DatapackRegistry;
import me.alfie.immersiveenchanting.api.datapack.ModDatapack;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeSoundsDatapack extends ModDatapack<NodeSoundMap, NodeSoundMap> {

    private NodeSoundMap data = new NodeSoundMap(new HashMap<>());

    public static void register(AddReloadListenerEvent event) {
        DatapackRegistry.register(event, NodeSoundsDatapack::new);
    }

    protected NodeSoundsDatapack() {
        super(NodeSoundMap.CODEC, DatapackKeys.NODE_SOUNDS, NodeSoundMap.STREAM_CODEC);
    }

    @Override
    public NodeSoundMap getData() {
        return data;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "node_sounds");
        data = parseOrDefault(map.get(key), new NodeSoundMap(new HashMap<>()));

        ImmersiveEnchanting.LOGGER.debug("Found {}", data);
    }
}
