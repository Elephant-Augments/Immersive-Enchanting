package me.alfie.immersiveenchanting.networking.packets;

import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public record GetBookshelfContentsPacket(int blockPosX, int blockPosY, int blockPosZ) implements CustomPacketPayload {
    public static final Type<GetBookshelfContentsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "checkbookshelfpacket"));

    public static final StreamCodec<ByteBuf, GetBookshelfContentsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            GetBookshelfContentsPacket::blockPosX,
            ByteBufCodecs.VAR_INT,
            GetBookshelfContentsPacket::blockPosY,
            ByteBufCodecs.VAR_INT,
            GetBookshelfContentsPacket::blockPosZ,
            GetBookshelfContentsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                GetBookshelfContentsPacket.TYPE,
                GetBookshelfContentsPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        null,
                        ServerPayloadHandler::onGetBookshelfContentsPacket
                )
        );
    }
}
