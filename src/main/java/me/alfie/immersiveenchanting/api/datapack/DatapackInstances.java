package me.alfie.immersiveenchanting.api.datapack;

import java.util.*;

/**
 * Stores all registered {@link ModDatapack} instances.
 *
 * <p>This acts as a central registry mapping {@link DatapackKey} → datapack instance.</p>
 *
 * <p>Note that this class stores the datapack objects themselves, NOT their processed data.
 * Processed data is handled separately by {@link DataMap}.</p>
 *
 * <p>Due to Java type erasure, datapacks are stored with wildcard types and cast back
 * when retrieved.</p>
 */
public final class DatapackInstances {

    /**
     * Internal storage of datapack instances.
     *
     * <p>Key = datapack identifier</p>
     * <p>Value = datapack instance</p>
     */
    private final Map<DatapackKey<?>, ModDatapack<?, ?>> DATAPACKS = new HashMap<>();

    /**
     * Registers a new datapack instance.
     *
     * <p>This should be called during datapack registration (typically during
     * {@code AddServerReloadListenersEvent}).</p>
     *
     * @param key      the datapack key
     * @param datapack the datapack instance
     * @param <T>      the datapack's output type
     */
    public <T> void register(DatapackKey<T> key, ModDatapack<?, T> datapack) {
        DATAPACKS.put(key, datapack);
    }

    /**
     * @return all registered datapack entries
     *
     * <p>This is primarily used internally for iterating over datapacks
     * during data collection and syncing.</p>
     */
    public Set<Map.Entry<DatapackKey<?>, ModDatapack<?, ?>>> entrySet() {
        return DATAPACKS.entrySet();
    }

    /**
     * Retrieves a datapack instance by its key.
     *
     * @param key the datapack key
     * @param <T> the datapack's output type
     * @return the datapack instance
     *
     * @throws ClassCastException if the stored datapack does not match the expected type
     */
    @SuppressWarnings("unchecked")
    public <T> ModDatapack<?, T> get(DatapackKey<T> key) {
        return (ModDatapack<?, T>) DATAPACKS.get(key);
    }
}
