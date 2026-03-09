package me.alfie.immersiveenchanting.events.datapack;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostDatapack;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.networking.packet.enchantmentcostregistrysync.EnchantmentCostRegistrySyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public class DatapackEvents {

    /**
     * Fires when tags are updated and expands tags in the enchantment cost data pack.
     * @param event
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        EnchantmentCostRegistry serverRegistry = EnchantmentCostRegistry.getServerRegistry();
        if(serverRegistry != null) {
            EnchantmentCostDatapack.expandTags(serverRegistry);
        }

        EnchantmentCostRegistry clientRegistry = EnchantmentCostRegistry.getClientRegistry();
        if(clientRegistry != null) {
            EnchantmentCostDatapack.expandTags(clientRegistry);
            //Client tags are also expanded during onEnchantmentCostRegistrySync()
        }
    }


    /**
     * Set the client for the datapack.
     * @param event
     */
    public static void setDatapackClient(FMLClientSetupEvent event) {
        EnchantmentCostRegistry.setClientRegistry(new EnchantmentCostRegistry());
    }

    /**
     * Set the server for the datapack.
     * @param event
     */
    @SubscribeEvent
    public static void setDatapackServer(ServerStartedEvent event) {
        EnchantmentCostDatapack.DATAPACK.setServer(event.getServer());
    }

    /**
     * Fires server-side. Adds a listener for datapack to update on /reload.
     * @param event
     */
    @SubscribeEvent
    public static void addInModDatapack(AddReloadListenerEvent event) {
        event.addListener(EnchantmentCostDatapack.DATAPACK);
    }

    /**
     * Request the server to send cost registry to this client.
     * @param event
     */
    @SubscribeEvent
    public static void syncClientRegistry(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            EnchantmentCostRegistrySyncPacket.syncClientWithServer(player);
        }
    }
}
