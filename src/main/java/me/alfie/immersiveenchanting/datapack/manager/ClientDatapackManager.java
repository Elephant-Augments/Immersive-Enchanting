package me.alfie.immersiveenchanting.datapack.manager;

import me.alfie.immersiveenchanting.api.datapack.DataMap;
import me.alfie.immersiveenchanting.api.datapack.DatapackInstances;
import me.alfie.immersiveenchanting.api.datapack.DatapackKey;
import me.alfie.immersiveenchanting.api.datapack.DatapackKeys;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

public class ClientDatapackManager {
    private static DataMap dataMap = new DataMap(new HashMap<>());

    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        if(dataMap.map().isEmpty()) return;
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            get(DatapackKeys.COST).resolveEnchantmentHolders(event.getLookupProvider());
        }
    }

    public static void setDataMap(DataMap newDataMap) {
        dataMap = newDataMap;
    }

    public static boolean isReady() {
        return !dataMap.map().isEmpty();
    }

    /**Get the stored data for this datapack key.*/
    public static <T> T get(DatapackKey<T> key) {
        return dataMap.get(key);
    }
}
