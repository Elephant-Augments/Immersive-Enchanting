package me.alfie.immersiveenchanting.datapack.manager;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.*;
import me.alfie.immersiveenchanting.networking.SyncClientDatapackPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class ServerDatapackManager {
    private static ServerDatapackManager INSTANCE;
    private static MinecraftServer SERVER;

    public static final DatapackInstances DATAPACKS = new DatapackInstances();
    private DataMap dataMap;

    public static void onServerStart(ServerAboutToStartEvent event) {
        SERVER = event.getServer();
        INSTANCE = new ServerDatapackManager();
        ImmersiveEnchanting.LOGGER.debug("ServerDatapackManager initialised.");
    }

    public static void onServerFinished(ServerStartedEvent event) {
        ImmersiveEnchanting.LOGGER.debug("Server finished loading... attempting to pull datapacks...");
        pullData();
    }

    public static void onServerReload(OnDatapackSyncEvent event) {
        if(event.getPlayer() == null) {
            ImmersiveEnchanting.LOGGER.debug("Server finished loading... attempting to pull datapacks...");
            pullData();

            for (ServerPlayer player : event.getPlayerList().getPlayers()) {
                sendSyncPacket(player);
            }
        } else {
            sendSyncPacket(event.getPlayer());
        }
    }

    public static void onServerStop(ServerStoppedEvent event) {
        SERVER = null;
        INSTANCE = null;

        ImmersiveEnchanting.LOGGER.debug("ServerDatapackManager uninitialised.");
    }

    /**Get the stored data for this datapack key.*/
    public static <T> T get(DatapackKey<T> key) {
        return getInstance().dataMap.get(key);
    }

    private static void pullData() {
        Map<DatapackKey<?>, Object> result = new HashMap<>();

        for (Map.Entry<DatapackKey<?>, ModDatapack<?, ?>> entry : DATAPACKS.entrySet()) {
            ModDatapack<?, ?> datapack = entry.getValue();

            result.put(entry.getKey(), datapack.getData());

            ImmersiveEnchanting.LOGGER.debug("Successfully pulled data for {}", datapack);
        }

        getInstance().dataMap = new DataMap(result); //Populate map

        for (Map.Entry<DatapackKey<?>, ModDatapack<?, ?>> entry : DATAPACKS.entrySet()) {
            ModDatapack<?, ?> datapack = entry.getValue();
            datapack.afterPull(getInstance().getServer()); //Process after
        }

    }

    /**Re-resolve holders on reload*/
    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            if(SERVER == null) return; //Prevent tags updating before server is ready.
            get(DatapackKeys.COST).resolveEnchantmentHolders(event.getLookupProvider());
        }
    }

    private static void sendSyncPacket(ServerPlayer player) {
        ImmersiveEnchanting.LOGGER.debug("Sending sync packet to {}...", player);

        PacketDistributor.sendToPlayer(player, new SyncClientDatapackPacket(getInstance().dataMap));
    }

    public static ServerDatapackManager getInstance() {
        if(INSTANCE == null) {
            throw new IllegalStateException("Server not started.");
        }
        return INSTANCE;
    }

    public MinecraftServer getServer() {
        return SERVER;
    }

}
