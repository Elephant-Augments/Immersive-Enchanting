package me.alfie.immersiveenchanting.networking.packet.transmutebookpacket;

import me.alfie.immersiveenchanting.networking.packet.NetworkPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record TransmuteBookPacket(int dummy) implements NetworkPacket<TransmuteBookPacket> {
    public static final Type<TransmuteBookPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "transmutebookpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmuteBookPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            TransmuteBookPacket::dummy, //Idk how to make an empty packet/ping packet - so im just passing a dummy int. :(
            TransmuteBookPacket::new
    );

    @Override
    public Type<TransmuteBookPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, TransmuteBookPacket> codec() {
        return STREAM_CODEC;
    }
}
