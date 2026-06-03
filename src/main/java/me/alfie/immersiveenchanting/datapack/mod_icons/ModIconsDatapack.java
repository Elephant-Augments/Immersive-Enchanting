package me.alfie.immersiveenchanting.datapack.mod_icons;

import me.alfie.alfinolib.datapacks.DatapackKey;
import me.alfie.alfinolib.datapacks.DatapackRegistry;
import me.alfie.alfinolib.datapacks.ModDatapack;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;

public class ModIconsDatapack extends ModDatapack<ModIconsMap, ModIconsMap> {

    public static final DatapackKey<ModIconsMap> KEY = new DatapackKey<>(ImmersiveEnchanting.MODID, "mod_icons");
    private ModIconsMap DATA = new ModIconsMap(new HashMap<>());

    protected ModIconsDatapack(RegistryAccess registryAccess) {
        super(ModIconsMap.CODEC, KEY, ModIconsMap.STREAM_CODEC, registryAccess);
    }

    @Override
    protected void apply(Map<Identifier, ModIconsMap> input, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ResourceId key = KEY.id();
        DATA = parseOrDefault(input.get(key), new ModIconsMap(new HashMap<>()));

        ImmersiveEnchanting.LOGGER.debug("Found {}", DATA);
    }

    @Override
    public ModIconsMap getData() {
        return DATA;
    }

    public static void register(AddServerReloadListenersEvent event) {
        DatapackRegistry.register(event, () -> new ModIconsDatapack(event.getRegistryAccess()));
    }
}
