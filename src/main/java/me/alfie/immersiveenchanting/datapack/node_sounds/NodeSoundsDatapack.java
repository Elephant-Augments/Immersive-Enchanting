package me.alfie.immersiveenchanting.datapack.node_sounds;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentCostData;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentCostDatapack;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public class NodeSoundsDatapack extends SimpleJsonResourceReloadListener<NodeSoundMap> {

    private static final String DIRECTORY = "sounds";
    public static Map<Identifier, NodeSound> map;

    protected NodeSoundsDatapack(Codec<NodeSoundMap> codec, FileToIdConverter lister) {
        super(codec, lister);
    }

    @Override
    protected void apply(Map<Identifier, NodeSoundMap> identifierNodeSoundMapMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        System.out.println(identifierNodeSoundMapMap.entrySet());

        Identifier key = Identifier.fromNamespaceAndPath("immersiveenchanting", "node_sounds");
        if(identifierNodeSoundMapMap.containsKey(key)) map = identifierNodeSoundMapMap.get(key).enchantments();
    }

    public static void registerServerDatapack(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY),
                new NodeSoundsDatapack(NodeSoundMap.CODEC, FileToIdConverter.json(DIRECTORY)));
    }

    public static void registerClientDatapack(AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, DIRECTORY),
                new NodeSoundsDatapack(NodeSoundMap.CODEC, FileToIdConverter.json(DIRECTORY)));
    }
}
