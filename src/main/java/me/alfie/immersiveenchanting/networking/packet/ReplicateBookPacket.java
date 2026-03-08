package me.alfie.immersiveenchanting.networking.packet;

import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public record ReplicateBookPacket(int dummy) implements CustomPacketPayload {
    public static final Type<ReplicateBookPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "replicatebookpacket"));

    public static final StreamCodec<FriendlyByteBuf, ReplicateBookPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ReplicateBookPacket::dummy, //Idk how to make an empty packet/ping packet - so im just passing a dummy int. :(
            ReplicateBookPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                ReplicateBookPacket.TYPE,
                ReplicateBookPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<ReplicateBookPacket>(
                        null,
                        ServerPayloadHandler::onReplicateBookPacket
                )
        );
    }
}
