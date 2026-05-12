package me.alfie.immersiveenchanting.api.datapack.manager;

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

/**
 * Server-side manager responsible for handling all datapack instances and their processed data.
 *
 * <p>This class:
 * <ul>
 *     <li>Stores all registered {@link ModDatapack} instances</li>
 *     <li>Builds a {@link DataMap} containing processed datapack data</li>
 *     <li>Handles datapack reload lifecycle events</li>
 *     <li>Synchronizes datapack data to connected clients</li>
 * </ul>
 *
 * <p>Datapack data is only guaranteed to be available after {@link #pullData()} has been called,
 * which occurs during server start and datapack reload.</p>
 */
public class ServerDatapackManager {

    /**Singleton instance of the server datapack manager.*/
    private static ServerDatapackManager INSTANCE;

    /**Reference to the active Minecraft server.*/
    private static MinecraftServer SERVER;

    /**
     * All registered datapack instances.
     *
     * <p>This is populated during datapack registration and contains the raw datapack objects
     * (not their processed data).</p>
     */
    public static final DatapackInstances DATAPACKS = new DatapackInstances();

    /**
     * Stores processed datapack data mapped by {@link DatapackKey}.
     *
     * <p>This is rebuilt whenever datapacks are loaded or reloaded.</p>
     */
    private DataMap dataMap;

    /**
     * Called when the server is about to start.
     *
     * <p>Initializes the manager and stores the server reference.</p>
     */
    public static void onServerStart(ServerAboutToStartEvent event) {
        SERVER = event.getServer();
        INSTANCE = new ServerDatapackManager();
        ImmersiveEnchanting.LOGGER.debug("ServerDatapackManager initialised.");
    }

    /**
     * Called once the server has fully started.
     *
     * <p>Builds the datapack data map for the first time.</p>
     */
    public static void onServerFinished(ServerStartedEvent event) {
        ImmersiveEnchanting.LOGGER.debug("Server finished loading... attempting to pull datapacks...");
        pullData();
    }

    /**
     * Called when datapacks are synced or reloaded.
     *
     * <p>If {@code player == null}, this is a full server reload:
     * <ul>
     *     <li>Rebuilds datapack data</li>
     *     <li>Syncs all players</li>
     * </ul>
     *
     * <p>If a player is present, only that player is synced.</p>
     */
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

    /**
     * Called when the server stops.
     *
     * <p>Clears stored references to prevent stale state.</p>
     */
    public static void onServerStop(ServerStoppedEvent event) {
        SERVER = null;
        INSTANCE = null;

        ImmersiveEnchanting.LOGGER.debug("ServerDatapackManager uninitialised.");
    }

    /**
     * Retrieves processed datapack data for the given key.
     *
     * @param key the datapack key
     * @param <T> the expected return type
     * @return the processed datapack data
     *
     * @throws IllegalStateException if called before datapacks are loaded
     */
    public static <T> T get(DatapackKey<T> key) {
        return getInstance().dataMap.get(key);
    }

    /**
     * Builds the {@link DataMap} from all registered datapacks.
     *
     * <p>This performs two stages:
     * <ol>
     *     <li>Collect raw processed data from each datapack</li>
     *     <li>Call {@link ModDatapack#afterPull(MinecraftServer)} for post-processing</li>
     * </ol>
     *
     * <p>This separation ensures all datapack data is available before any cross-datapack logic runs.</p>
     */
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

    /**
     * Re-resolves enchantment holders when tag data is updated from a server data load.
     * Guards against running before the server is ready.
     */
    public static void resolveEnchantmentHolders(TagsUpdatedEvent event) {
        if(event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            if(SERVER == null) return; //Prevent tags updating before server is ready.
            get(DatapackKeys.COST).resolveEnchantmentHolders(event.getLookupProvider());
        }
    }

    /**
     * Sends the current datapack data to a player.
     *
     * @param player the player to sync with
     */
    private static void sendSyncPacket(ServerPlayer player) {
        ImmersiveEnchanting.LOGGER.debug("Sending sync packet to {}...", player);

        PacketDistributor.sendToPlayer(player, new SyncClientDatapackPacket(getInstance().dataMap));
    }

    /**
     * @return the current server datapack manager instance
     * @throws IllegalStateException if the server has not started
     */
    public static ServerDatapackManager getInstance() {
        if(INSTANCE == null) {
            throw new IllegalStateException("Server not started.");
        }
        return INSTANCE;
    }

    /**
     * @return the active Minecraft server instance
     */
    public MinecraftServer getServer() {
        return SERVER;
    }

}
