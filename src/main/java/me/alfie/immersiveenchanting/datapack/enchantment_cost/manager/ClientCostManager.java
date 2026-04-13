package me.alfie.immersiveenchanting.datapack.enchantment_cost.manager;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import net.minecraft.core.HolderLookup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

public class ClientCostManager {
    private static CostRegistry registry = new CostRegistry();

    public static void setRegistry(CostRegistry newRegistry, HolderLookup.Provider lookup) {
        registry = newRegistry;
        CostRegistry.resolveEnchantmentHolders(registry(), lookup);
    }

    public static CostRegistry registry() {
        return registry;
    }

    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
            CostRegistry.resolveEnchantmentHolders(registry(), event.getLookupProvider());
        }
    }
}
