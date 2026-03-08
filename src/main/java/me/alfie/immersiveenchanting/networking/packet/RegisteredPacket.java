package me.alfie.immersiveenchanting.networking.packet;

import me.alfie.immersiveenchanting.networking.payload.PayloadHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public interface RegisteredPacket<T extends CustomPacketPayload> extends CustomPacketPayload {

    CustomPacketPayload.Type<T> typeId();

    StreamCodec<RegistryFriendlyByteBuf, T> codec();

    PayloadHandler<T> handler();

    default void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        PayloadHandler<T> h = handler();

        registrar.playBidirectional(
                typeId(),
                codec(),
                new DirectionalPayloadHandler<>(
                        h::execOnClient,
                        h::execOnServer
                )
        );
    }

    @Override
    default Type<? extends CustomPacketPayload> type() {
        return typeId();
    }
}
