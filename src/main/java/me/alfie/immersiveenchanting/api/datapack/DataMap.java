package me.alfie.immersiveenchanting.api.datapack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable container for all processed datapack data.
 *
 * <p>This maps {@link DatapackKey} → processed data ({@code T}).</p>
 *
 * <p>Each key determines the expected type of its value:
 * <pre>
 * DatapackKey&lt;CostRegistry&gt; → CostRegistry
 * DatapackKey&lt;NodeSoundMap&gt; → NodeSoundMap
 * </pre>
 *
 * <p>Due to Java type erasure, values are stored internally as {@link Object}
 * and cast back to their expected type when accessed.</p>
 *
 * <p>This class also handles network serialization for syncing datapack data
 * from server to client.</p>
 */
public record DataMap(Map<DatapackKey<?>, Object> map) {

    /**
     * Stream codec used to serialize and deserialize the entire datapack data map.
     *
     * <p>This delegates encoding/decoding of individual values to the corresponding
     * {@link ModDatapack}'s {@link StreamCodec}.</p>
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, DataMap> STREAM_CODEC = StreamCodec.of(
            DataMap::encode,
            DataMap::decode
    );

    /**
     * Creates an immutable view of the provided map.
     *
     * <p>The internal map cannot be modified after creation.</p>
     */
    public DataMap {
        map = Collections.unmodifiableMap(map);
    }

    /**
     * Encodes the datapack data map into the buffer.
     *
     * <p>For each entry:
     * <ol>
     *     <li>Write the {@link DatapackKey}</li>
     *     <li>Look up the corresponding {@link ModDatapack}</li>
     *     <li>Use its {@link StreamCodec} to encode the value</li>
     * </ol>
     *
     * <p>This relies on the server having access to all registered datapacks.</p>
     */
    @SuppressWarnings("unchecked")
    private static <T> void encode(RegistryFriendlyByteBuf buf, DataMap dataMap) {
        buf.writeVarInt(dataMap.map().size());

        for (Map.Entry<DatapackKey<?>, Object> entry : dataMap.map().entrySet()) {
            DatapackKey<T> key = (DatapackKey<T>) entry.getKey();
            T value = (T) entry.getValue();

            DatapackKey.STREAM_CODEC.encode(buf, key);
            ModDatapack<?, T> datapack = DatapackRegistry.getDatapacks().get(key);

            StreamCodec<RegistryFriendlyByteBuf, T> codec = datapack.codec();
            codec.encode(buf, value);
        }
    }

    /**
     * Decodes a datapack data map from the buffer.
     *
     * <p>For each entry:
     * <ol>
     *     <li>Read the {@link DatapackKey}</li>
     *     <li>Look up the corresponding {@link ModDatapack}</li>
     *     <li>Use its {@link StreamCodec} to decode the value</li>
     * </ol>
     *
     * <p>This requires that the client has already registered the same datapacks
     * as the server, otherwise decoding will fail.</p>
     */
    private static DataMap decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<DatapackKey<?>, Object> result = new HashMap<>();

        for (int i = 0; i < size; i++) {
            DatapackKey<?> datapackKey = DatapackKey.STREAM_CODEC.decode(buf);
            ModDatapack<?, ?> datapack = DatapackRegistry.getDatapacks().get(datapackKey);

            StreamCodec<RegistryFriendlyByteBuf, ?> codec = datapack.codec();
            Object value = codec.decode(buf);

            result.put(datapackKey, value);
        }

        return new DataMap(result);
    }

    /**
     * Retrieves datapack data for the given key.
     *
     * @param key the datapack key
     * @param <T> the expected return type
     * @return the stored datapack data
     * @throws ClassCastException if the stored value does not match the expected type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(DatapackKey<T> key) {
        return (T) map.get(key);
    }
}
