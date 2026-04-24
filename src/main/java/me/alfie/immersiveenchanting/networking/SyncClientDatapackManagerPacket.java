package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import me.alfie.immersiveenchanting.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record SyncClientDatapackManagerPacket(CostRegistry costRegistry,
                                              NodeSoundMap nodeSoundMap) implements ModNetworkPacket<SyncClientDatapackManagerPacket> {

    public static final Type<@NotNull SyncClientDatapackManagerPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "sync_client_cost_manager_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncClientDatapackManagerPacket> STREAM_CODEC = StreamCodec.composite(
            CostRegistry.STREAM_CODEC, SyncClientDatapackManagerPacket::costRegistry,
            NodeSoundMap.STREAM_CODEC, SyncClientDatapackManagerPacket::nodeSoundMap,

            SyncClientDatapackManagerPacket::new
    );

    @Override
    public Type<@NotNull SyncClientDatapackManagerPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SyncClientDatapackManagerPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(SyncClientDatapackManagerPacket packet, IPayloadContext context) {
        CostRegistry costRegistry = packet.costRegistry();
        NodeSoundMap nodeSoundMap = packet.nodeSoundMap();

        ClientDatapackManager.setCostRegistry(costRegistry, context.player().registryAccess());
        ClientDatapackManager.setNodeSoundMap(nodeSoundMap);

        ImmersiveEnchanting.LOGGER.debug("Received sync packet on client, updated client datapack manager.");
        costRegistry.printRegistry();
    }
}

