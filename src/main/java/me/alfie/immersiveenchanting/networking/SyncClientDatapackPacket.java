package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.datapack.DataMap;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.api.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Server-to-client packet that synchronizes datapack-driven client state.
 *
 * <p>Updates the client-side {@link CostRegistry} and {@link NodeSoundMap} so that
 * enchantment costs and node sound mappings match the server configuration.</p>
 *
 * <p>This packet is typically sent on login or datapack reload to ensure the client
 * has an up-to-date view of gameplay rules and effects.</p>
 */
public record SyncClientDatapackPacket(DataMap dataMap) implements ModNetworkPacket<SyncClientDatapackPacket> {

    public static final Type<@NotNull SyncClientDatapackPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "sync_client_datapack_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncClientDatapackPacket> STREAM_CODEC = StreamCodec.composite(
            DataMap.STREAM_CODEC, SyncClientDatapackPacket::dataMap,
            SyncClientDatapackPacket::new
    );

    @Override
    public Type<@NotNull SyncClientDatapackPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SyncClientDatapackPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(SyncClientDatapackPacket packet, IPayloadContext context) {
        DataMap dataMap = packet.dataMap();

        ClientDatapackManager.setDataMap(dataMap);

        ImmersiveEnchanting.LOGGER.debug("Received sync packet on client, updated client datapack manager.");
    }
}

