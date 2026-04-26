package me.alfie.immersiveenchanting.datapack.manager;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.networking.SyncClientDatapackManagerPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;

public class ServerDatapackManager {

    private static ServerDatapackManager INSTANCE;
    private static MinecraftServer SERVER;
    private CostRegistry costRegistry = new CostRegistry();
    private NodeSoundMap nodeSoundMap = new NodeSoundMap(new HashMap<>());

    public static void onServerStart(ServerAboutToStartEvent event) {
        SERVER = event.getServer();
        INSTANCE = new ServerDatapackManager();

        ImmersiveEnchanting.LOGGER.debug("ServerDatapackManager initialised.");
    }

    public static void onServerFinished(ServerStartedEvent event) {
        ImmersiveEnchanting.LOGGER.debug("Server finished loading... attempting to pull built cost registry.");

        pullBuiltCostRegistry();
        costRegistry().resolveEnchantmentHolders(event.getServer().registryAccess());

        ImmersiveEnchanting.LOGGER.debug("Server finished loading... attempting to pull built node sounds map.");
        pullBuiltNodeSoundsMap();
    }

    public static void onServerReload(OnDatapackSyncEvent event) {
        if(event.getPlayer() == null) {
            ImmersiveEnchanting.LOGGER.debug("Server reloading... attempting to pull built cost registry.");
            pullBuiltCostRegistry();
            costRegistry().resolveEnchantmentHolders(SERVER.registryAccess());

            ImmersiveEnchanting.LOGGER.debug("Server reloading... attempting to pull built node sounds map.");
            pullBuiltNodeSoundsMap();

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
            costRegistry().resolveEnchantmentHolders(event.getRegistryAccess());
        }
    }

    private static void sendSyncPacket(ServerPlayer player) {
        ImmersiveEnchanting.LOGGER.debug("Sending sync packet to {}...", player);

        PacketDistributor.sendToPlayer(player, new SyncClientDatapackManagerPacket(
                costRegistry(),
                nodeSoundMap()));
    }

    public static void onServerStop(ServerStoppedEvent event) {
        SERVER = null;
        INSTANCE = null;

        ImmersiveEnchanting.LOGGER.debug("ServerDatapackManager uninitialised.");
    }

    private static void pullBuiltCostRegistry() {
        getInstance().costRegistry = CostDatapack.getInstance().getBuilt();

        ImmersiveEnchanting.LOGGER.debug("Successfully pulled built cost registry from datapack to ServerDatapackManager :)");
        costRegistry().printRegistry();
    }

    private static void pullBuiltNodeSoundsMap() {
        getInstance().nodeSoundMap = NodeSoundsDatapack.getInstance().getBuilt();

        ImmersiveEnchanting.LOGGER.debug("Successfully pulled built node sound map from datapack to ServerDatapackManager :)");
    }

    public static ServerDatapackManager getInstance() {
        if(INSTANCE == null) {
            throw new IllegalStateException("Server not started.");
        }
        return INSTANCE;
    }

    public static CostRegistry costRegistry() {
        return getInstance().getCostRegistry();
    }

    public static NodeSoundMap nodeSoundMap() { return getInstance().getNodeSoundMap(); }

    public MinecraftServer getServer() {
        return SERVER;
    }

    public CostRegistry getCostRegistry() {
        return costRegistry;
    }

    public NodeSoundMap getNodeSoundMap() { return nodeSoundMap; }


}
