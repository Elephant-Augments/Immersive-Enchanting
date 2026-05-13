package me.alfie.immersiveenchanting.api.datapack;

import me.alfie.immersiveenchanting.api.datapack.manager.ServerDatapackManager;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.function.Supplier;

/**
 * Utility class for registering {@link ModDatapack} instances.
 *
 * <p>This handles both:
 * <ul>
 *     <li>Registering the datapack as a reload listener with NeoForge</li>
 *     <li>Registering the datapack internally for later access and syncing</li>
 * </ul>
 *
 * <p>This should be called during {@link AddServerReloadListenersEvent}.</p>
 */
public class DatapackRegistry {

    /**
     * All registered datapack instances.
     *
     * <p>This is populated during datapack registration and contains the raw datapack objects
     * (not their processed data).</p>
     */
    private static final DatapackInstances DATAPACKS = new DatapackInstances();

    /**
     * Registers a {@link ModDatapack}.
     *
     * <p>This will:
     * <ol>
     *     <li>Create a new datapack instance using the supplied factory</li>
     *     <li>Register it as a reload listener (so it loads on /reload)</li>
     *     <li>Store it in {@link ServerDatapackManager#DATAPACKS} for later use</li>
     * </ol>
     *
     * <p>The {@link Supplier} is used instead of passing an instance directly to
     * avoid unnecessary duplication and to keep registration concise.</p>
     *
     * @param event   the reload listener registration event
     * @param factory factory that creates the datapack instance
     * @param <C>     the raw data type decoded from JSON (Codec type)
     * @param <T>     the processed data type returned by the datapack
     */
    public static <C, T> void register(AddServerReloadListenersEvent event, Supplier<ModDatapack<C, T>> factory) {
        ModDatapack<C, T> datapack = factory.get();
        DatapackKey<T> datapackKey = datapack.key();

        event.addListener(
                Identifier.fromNamespaceAndPath(datapackKey.modid(), datapackKey.directory()),
                datapack);

        DATAPACKS.register(datapackKey, datapack);
    }

    public static DatapackInstances getDatapacks() {
        return DATAPACKS;
    }
}
