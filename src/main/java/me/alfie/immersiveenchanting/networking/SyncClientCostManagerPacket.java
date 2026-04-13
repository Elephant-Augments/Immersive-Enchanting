package me.alfie.immersiveenchanting.networking;

import ca.weblite.objc.Client;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.codec.CostData;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.manager.ClientCostManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SyncClientCostManagerPacket(Map<Identifier, CostData> idRegistry) implements ModNetworkPacket<SyncClientCostManagerPacket> {

    public static final Type<@NotNull SyncClientCostManagerPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "sync_client_cost_manager_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncClientCostManagerPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    Identifier.STREAM_CODEC,
                    CostData.STREAM_CODEC
            ),
            SyncClientCostManagerPacket::idRegistry,
            SyncClientCostManagerPacket::new
    );

    @Override
    public Type<@NotNull SyncClientCostManagerPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SyncClientCostManagerPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(SyncClientCostManagerPacket packet, IPayloadContext context) {
        CostRegistry registry = new CostRegistry(packet.idRegistry());
        ClientCostManager.setRegistry(registry, context.player().registryAccess());

        ImmersiveEnchanting.LOGGER.debug("Received sync packet on client, updated client cost registry.");
        registry.printRegistry();
    }
}

