package me.alfie.immersiveenchanting.datapack.manager;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashMap;

public class ClientDatapackManager {
    private static CostRegistry costRegistry = new CostRegistry();
    private static NodeSoundMap nodeSoundMap = new NodeSoundMap(new HashMap<>());

    public static void setCostRegistry(CostRegistry newRegistry, HolderLookup.Provider lookup) {
        costRegistry = newRegistry;
        costRegistry().resolveEnchantmentHolders(lookup);
    }

    public static CostRegistry costRegistry() {
        return costRegistry;
    }

    public static void setNodeSoundMap(NodeSoundMap newNodeSoundMap) {
        nodeSoundMap = newNodeSoundMap;
    }

    public static NodeSoundMap nodeSoundMap() {
        return nodeSoundMap;
    }

    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            costRegistry().resolveEnchantmentHolders(event.getRegistryAccess());
        }
    }
}
