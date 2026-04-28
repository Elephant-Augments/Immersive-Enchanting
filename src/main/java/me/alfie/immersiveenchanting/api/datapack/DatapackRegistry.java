package me.alfie.immersiveenchanting.api.datapack;

import me.alfie.immersiveenchanting.datapack.manager.ServerDatapackManager;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.function.Supplier;

public class DatapackRegistry {

    /**
     * Register a ModDatapack to the server.
     * @param event
     * @param factory
     * @param <C>
     * @param <T>
     */
    public static <C, T> void register(AddServerReloadListenersEvent event, Supplier<ModDatapack<C, T>> factory) {
        ModDatapack<C, T> datapack = factory.get();
        DatapackKey<T> datapackKey = datapack.key();

        event.addListener(
                Identifier.fromNamespaceAndPath(datapackKey.modid(), datapackKey.directory()),
                datapack);

        ServerDatapackManager.DATAPACKS.register(datapackKey, datapack);
    }
}
