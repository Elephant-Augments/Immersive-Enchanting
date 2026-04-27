package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeSoundsDatapack extends SimpleJsonResourceReloadListener {

    private static NodeSoundsDatapack INSTANCE;
    private static final String DIRECTORY = "sounds";
    private NodeSoundMap TEMP = new NodeSoundMap(new HashMap<>());

    protected NodeSoundsDatapack(Gson gson, String directory) {
        super(gson, directory);
    }

    public static NodeSoundsDatapack getInstance() {
        return INSTANCE;
    }

    public NodeSoundMap getBuilt() {
        return TEMP;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "node_sounds");

        JsonElement json = map.get(key);

        if(json != null) {
            TEMP = NodeSoundMap.CODEC
                    .parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> ImmersiveEnchanting.LOGGER.error("Failled to parse {}: {}", key, error))
                    .orElse(new NodeSoundMap(new HashMap<>()));
        } else {
            TEMP = new NodeSoundMap(new HashMap<>());
        }
    }

    public static void registerServerDatapack(AddReloadListenerEvent event) {
        NodeSoundsDatapack datapack = new NodeSoundsDatapack(new Gson(), DIRECTORY);
        INSTANCE = datapack;

        event.addListener(datapack);
    }



}
