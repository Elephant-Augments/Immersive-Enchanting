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

public record TransmuteBookPacket(int dummy) implements CustomPacketPayload {
    public static final Type<TransmuteBookPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "transmutebookpacket"));

    public static final StreamCodec<FriendlyByteBuf, TransmuteBookPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TransmuteBookPacket::dummy, //Idk how to make an empty packet/ping packet - so im just passing a dummy int. :(
            TransmuteBookPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                TransmuteBookPacket.TYPE,
                TransmuteBookPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<TransmuteBookPacket>(
                        null,
                        ServerPayloadHandler::onTransmuteBookPacket
                )
        );
    }
}
