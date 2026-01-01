package me.alfie.immersiveenchanting.networking.packets;

import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public record UpdateToolSlotPacket(int mode) implements CustomPacketPayload {
    public static final Type<UpdateToolSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "updatetoolslotpacket"));

    public static enum MODE {
        TAKE,
        PLACE
    }

    public static final StreamCodec<FriendlyByteBuf, UpdateToolSlotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateToolSlotPacket::mode,
            UpdateToolSlotPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                UpdateToolSlotPacket.TYPE,
                UpdateToolSlotPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<UpdateToolSlotPacket>(
                        null,
                        ServerPayloadHandler::onUpdateSlotPacket
                )
        );
    }
}
