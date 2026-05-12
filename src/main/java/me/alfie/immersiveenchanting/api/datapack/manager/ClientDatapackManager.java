package me.alfie.immersiveenchanting.api.datapack.manager;

import me.alfie.immersiveenchanting.api.datapack.DataMap;
import me.alfie.immersiveenchanting.api.datapack.DatapackKey;
import me.alfie.immersiveenchanting.datapack.DatapackKeys;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashMap;

/**
 * Client-side manager for synced datapack data.
 *
 * <p>This class stores datapack data received from the server via
 * {@code SyncClientDatapackPacket} and provides access to it.</p>
 *
 * <p>Unlike the server, the client does NOT load datapacks itself.
 * All data is received over the network and may not be immediately available.</p>
 *
 * <p>Use {@link #isReady()} to check whether data has been received before accessing it.</p>
 */
public class ClientDatapackManager {
    /**
     * Stores all synced datapack data from the server.
     *
     * <p>This is replaced whenever a sync packet is received.</p>
     */
    private static DataMap dataMap = new DataMap(new HashMap<>());

    /**
     * Internal method for CostDatapack
     * Re-resolves enchantment holders after the client receives updated tags.
     * Only runs on {@link TagsUpdatedEvent.UpdateCause#CLIENT_PACKET_RECEIVED} and
     * no-ops if no sync packet has been received yet.
     */
    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        if(!isReady()) return;
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            get(DatapackKeys.COST).resolveEnchantmentHolders(event.getLookupProvider());
        }
    }

    /**
     * Replaces the client-side datapack data with the map received from the server.
     * Called when a {@link me.alfie.immersiveenchanting.networking.SyncClientDatapackPacket} is processed.
     */
    public static void setDataMap(DataMap newDataMap) {
        dataMap = newDataMap;
    }

    /**
     * @return {@code true} if datapack data has been received and is ready for use
     */
    public static boolean isReady() {
        return !dataMap.map().isEmpty();
    }

    /**
     * Retrieves datapack data for the given key.
     *
     * @param key the datapack key
     * @param <T> the expected return type
     * @return the synced datapack data
     *
     * @throws ClassCastException if the expected type does not match the stored data
     */
    public static <T> T get(DatapackKey<T> key) {
        return dataMap.get(key);
    }
}
