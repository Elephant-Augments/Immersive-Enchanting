package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.google.gson.JsonElement;
import me.alfie.alfinolib.datapacks.DatapackKey;
import me.alfie.alfinolib.datapacks.DatapackRegistry;
import me.alfie.alfinolib.datapacks.ModDatapack;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeSoundsDatapack extends ModDatapack<NodeSoundMap, NodeSoundMap> {

    public static DatapackKey<NodeSoundMap> KEY = new DatapackKey<>(ImmersiveEnchanting.MODID, "sounds");
    private NodeSoundMap DATA = new NodeSoundMap(new HashMap<>());

    protected NodeSoundsDatapack(RegistryAccess registryAccess) {
        super(NodeSoundMap.CODEC, KEY, NodeSoundMap.STREAM_CODEC, registryAccess);
    }

    @Override public NodeSoundMap getData() {
        return DATA;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "node_sounds");
        DATA = parseOrDefault(input.get(key), new NodeSoundMap(new HashMap<>()));

        ImmersiveEnchanting.LOGGER.debug("Found {}", String.valueOf(DATA));
    }

    public static void register(AddReloadListenerEvent event) {
        DatapackRegistry.register(event, () -> new NodeSoundsDatapack(event.getRegistryAccess()));
    }
}
