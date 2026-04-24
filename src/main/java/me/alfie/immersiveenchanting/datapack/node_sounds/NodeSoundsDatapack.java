package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeSoundsDatapack extends SimpleJsonResourceReloadListener<NodeSoundMap> {

    private static NodeSoundsDatapack INSTANCE;
    private static final String DIRECTORY = "sounds";
    private NodeSoundMap TEMP = new NodeSoundMap(new HashMap<>());

    protected NodeSoundsDatapack(Codec<NodeSoundMap> codec, FileToIdConverter lister) {
        super(codec, lister);
    }

    public static NodeSoundsDatapack getInstance() {
        return INSTANCE;
    }

    public NodeSoundMap getBuilt() {
        return TEMP;
    }

    @Override
    protected void apply(Map<Identifier, NodeSoundMap> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Identifier key = Identifier.fromNamespaceAndPath("immersiveenchanting", "node_sounds");
        TEMP = input.getOrDefault(
                key,
                new NodeSoundMap(new HashMap<>())
        );
    }

    public static void registerServerDatapack(AddServerReloadListenersEvent event) {
        NodeSoundsDatapack datapack = new NodeSoundsDatapack(NodeSoundMap.CODEC, FileToIdConverter.json(DIRECTORY));
        INSTANCE = datapack;

        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY), datapack);
    }


}
