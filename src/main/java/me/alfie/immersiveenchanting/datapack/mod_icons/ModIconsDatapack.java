package me.alfie.immersiveenchanting.datapack.mod_icons;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DatapackRegistry;
import me.alfie.immersiveenchanting.api.datapack.ModDatapack;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

public class ModIconsDatapack extends ModDatapack<ModIconsMap, ModIconsMap> {

    private ModIconsMap data = new ModIconsMap(new HashMap<>());

    public static void register(AddServerReloadListenersEvent event) {
        DatapackRegistry.register(event, ModIconsDatapack::new);
    }

    protected ModIconsDatapack() {
        super(ModIconsMap.CODEC, DatapackKeys.MOD_ICONS, ModIconsMap.STREAM_CODEC);
    }

    @Override
    public ModIconsMap getData() {
        return data;
    }

    @Override
    protected void apply(Map<ResourceLocation, ModIconsMap> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ResourceLocation key = DatapackKeys.MOD_ICONS.ResourceLocation();
        data = input.getOrDefault(key, new ModIconsMap(new HashMap<>()));

        ImmersiveEnchanting.LOGGER.debug("Found {}", data);
    }
}
