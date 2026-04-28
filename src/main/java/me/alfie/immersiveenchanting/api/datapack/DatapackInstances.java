package me.alfie.immersiveenchanting.api.datapack;

import java.util.*;

public final class DatapackInstances {

    private final Map<DatapackKey<?>, ModDatapack<?, ?>> DATAPACKS = new HashMap<>();

    /**Register a new instance of a datapack*/
    public <T> void register(DatapackKey<T> key, ModDatapack<?, T> datapack) {
        DATAPACKS.put(key, datapack);
    }

    public Set<Map.Entry<DatapackKey<?>, ModDatapack<?, ?>>> entrySet() {
        return DATAPACKS.entrySet();
    }

    @SuppressWarnings("unchecked")
    public <T> ModDatapack<?, T> get(DatapackKey<T> key) {
        return (ModDatapack<?, T>) DATAPACKS.get(key);
    }
}
