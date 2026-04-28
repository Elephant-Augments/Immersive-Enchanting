package me.alfie.immersiveenchanting.api.datapack;

import me.alfie.immersiveenchanting.datapack.manager.ServerDatapackManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record DataMap(Map<DatapackKey<?>, Object> map) {

    public DataMap {
        map = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(DatapackKey<T> key) {
        return (T) map.get(key);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, DataMap> STREAM_CODEC = StreamCodec.of(
            DataMap::encode,
            DataMap::decode
    );

    @SuppressWarnings("unchecked")
    private static <T> void encode(RegistryFriendlyByteBuf buf, DataMap dataMap) {
        buf.writeVarInt(dataMap.map().size());

        for (Map.Entry<DatapackKey<?>, Object> entry : dataMap.map().entrySet()) {
            DatapackKey<T> key = (DatapackKey<T>) entry.getKey();
            T value = (T) entry.getValue();

            DatapackKey.STREAM_CODEC.encode(buf, key);
            ModDatapack<?, T> datapack = ServerDatapackManager.DATAPACKS.get(key);

            StreamCodec<RegistryFriendlyByteBuf, T> codec = datapack.codec();
            codec.encode(buf, value);
        }
    }

    private static DataMap decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<DatapackKey<?>, Object> result = new HashMap<>();

        for (int i = 0; i < size; i++) {
            DatapackKey<?> datapackKey = DatapackKey.STREAM_CODEC.decode(buf);
            ModDatapack<?, ?> datapack = ServerDatapackManager.DATAPACKS.get(datapackKey);

            StreamCodec<RegistryFriendlyByteBuf, ?> codec = datapack.codec();
            Object value = codec.decode(buf);

            result.put(datapackKey, value);
        }

        return new DataMap(result);
    }
}
