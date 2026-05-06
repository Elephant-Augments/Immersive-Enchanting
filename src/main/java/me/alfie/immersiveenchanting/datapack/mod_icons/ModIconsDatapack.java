package me.alfie.immersiveenchanting.datapack.mod_icons;

import com.mojang.serialization.Codec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DatapackKey;
import me.alfie.immersiveenchanting.api.datapack.DatapackKeys;
import me.alfie.immersiveenchanting.api.datapack.DatapackRegistry;
import me.alfie.immersiveenchanting.api.datapack.ModDatapack;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

public class ModIconsDatapack extends ModDatapack<ModIconsMap, ModIconsMap> {

    private ModIconsMap data = new ModIconsMap(new HashMap<>());

    protected ModIconsDatapack() {
        super(ModIconsMap.CODEC, DatapackKeys.MOD_ICONS, ModIconsMap.STREAM_CODEC);
    }

    @Override
    protected void apply(Map<Identifier, ModIconsMap> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Identifier key = DatapackKeys.MOD_ICONS.identifier();
        data = input.getOrDefault(key, new ModIconsMap(new HashMap<>()));

        ImmersiveEnchanting.LOGGER.debug("Found {}", data);
    }

    @Override
    public ModIconsMap getData() {
        return data;
    }

    public static void register(AddServerReloadListenersEvent event) {
        DatapackRegistry.register(event, ModIconsDatapack::new);
    }
}
