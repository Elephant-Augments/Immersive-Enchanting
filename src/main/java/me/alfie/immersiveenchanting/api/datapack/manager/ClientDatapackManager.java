package me.alfie.immersiveenchanting.api.datapack.manager;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DataMap;
import me.alfie.immersiveenchanting.api.datapack.DatapackKey;
import me.alfie.immersiveenchanting.api.datapack.internal.DatapackKeys;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.common.NeoForge;
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
 */
public class ClientDatapackManager {
    /**
     * Stores all synced datapack data from the server.
     *
     * <p>This is replaced whenever a sync packet is received.</p>
     */
    private static DataMap dataMap = new DataMap(new HashMap<>());

    /**
     * Replaces the client-side datapack data with the map received from the server.
     * Called when a {@link me.alfie.immersiveenchanting.networking.SyncClientDatapackPacket} is processed.
     */
    public static void setDataMap(DataMap newDataMap) {
        dataMap = newDataMap;
        NeoForge.EVENT_BUS.post(new ClientDatapackUpdatedEvent(Minecraft.getInstance().player));
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
