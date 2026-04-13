package me.alfie.immersiveenchanting.networking;

import com.mojang.datafixers.types.templates.Check;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {

    /**
     * Register payloads inbound to server.
     * @param event
     */
    public static void registerServer(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(UpdateToolSlotPacket.TYPE, UpdateToolSlotPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToServer(CheckBookshelvesPacket.TYPE, CheckBookshelvesPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToClient(AvailableEnchantmentsPacket.TYPE, AvailableEnchantmentsPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

        registrar.playToClient(SyncClientCostManagerPacket.TYPE, SyncClientCostManagerPacket.STREAM_CODEC,
                (packet, context) -> packet.exec(packet, context));

    }

    /**
     * Register payloads inbound to client.
     * @param event
     */
    public static void registerClient(RegisterClientPayloadHandlersEvent event) {

    }
}
