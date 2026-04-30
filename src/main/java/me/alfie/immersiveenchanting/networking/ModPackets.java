package me.alfie.immersiveenchanting.networking;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Enchantment>> ENCHANTMENT_HOLDER_CODEC = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT);

    /**
     * Register payloads inbound to server.
     * @param event
     */
    public static void registerServer(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(UpdateToolSlotPacket.TYPE, UpdateToolSlotPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToServer(EnchantPacket.TYPE, EnchantPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToServer(TransmutePacket.TYPE, TransmutePacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToServer(ReplicatePacket.TYPE, ReplicatePacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToServer(RemoveEnchantmentPacket.TYPE, RemoveEnchantmentPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToClient(AvailableEnchantmentsPacket.TYPE, AvailableEnchantmentsPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToClient(SyncClientDatapackPacket.TYPE, SyncClientDatapackPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

    }

    /**
     * Register payloads inbound to client.
     * @param event
     */
    public static void registerClient(RegisterClientPayloadHandlersEvent event) {

    }
}
