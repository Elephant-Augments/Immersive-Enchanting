package me.alfie.immersiveenchanting.datapack.enchantment_cost.manager;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.networking.SyncClientCostManagerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class ServerCostManager {

    private static ServerCostManager INSTANCE;
    private static MinecraftServer SERVER;
    private CostRegistry registry = new CostRegistry();

    public static void onServerStart(ServerAboutToStartEvent event) {
        SERVER = event.getServer();
        INSTANCE = new ServerCostManager();

        ImmersiveEnchanting.LOGGER.debug("ServerCostManager initialised.");
    }

    public static void onServerFinished(ServerStartedEvent event) {
        ImmersiveEnchanting.LOGGER.debug("Server finished loading... attempting to pull built cost registry.");

        pullBuiltRegistry();
        CostRegistry.resolveEnchantmentHolders(registry(), event.getServer().registryAccess());
    }

    public static void onServerReload(OnDatapackSyncEvent event) {
        if(event.getPlayer() == null) {
            ImmersiveEnchanting.LOGGER.debug("Server reloading... attempting to pull built cost registry.");
            pullBuiltRegistry();

            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                sendSyncPacket(player);
            }
        } else {
            sendSyncPacket(event.getPlayer());
        }
    }

    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            if(SERVER == null) return; //Prevent tags updating before server is ready.
            CostRegistry.resolveEnchantmentHolders(registry(), event.getLookupProvider());
        }
    }

    private static void sendSyncPacket(ServerPlayer player) {
        ImmersiveEnchanting.LOGGER.debug("Sending sync packet to {}...", player);

        PacketDistributor.sendToPlayer(player, new SyncClientCostManagerPacket(registry().snapshotIdRegistry()));
    }

    public static void onServerStop(ServerStoppedEvent event) {
        SERVER = null;
        INSTANCE = null;

        ImmersiveEnchanting.LOGGER.debug("ServerCostManager uninitialised.");
    }

    private static void pullBuiltRegistry() {
        getInstance().registry = CostDatapack.getInstance().getBuiltRegistry();

        ImmersiveEnchanting.LOGGER.debug("Successfully pulled built cost registry from datapack to ServerCostManager :)");
        registry().printRegistry();
    }

    public static ServerCostManager getInstance() {
        if(INSTANCE == null) {
            throw new IllegalStateException("Server not started.");
        }
        return INSTANCE;
    }

    public static CostRegistry registry() {
        return getInstance().getRegistry();
    }

    public MinecraftServer getServer() {
        return SERVER;
    }

    public CostRegistry getRegistry() {
        return registry;
    }


}
