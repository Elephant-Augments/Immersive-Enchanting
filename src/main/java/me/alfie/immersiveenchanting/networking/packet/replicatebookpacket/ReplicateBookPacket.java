package me.alfie.immersiveenchanting.networking.packet.replicatebookpacket;

import me.alfie.immersiveenchanting.networking.packet.NetworkPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record ReplicateBookPacket(int dummy) implements NetworkPacket<ReplicateBookPacket> {
    public static final Type<ReplicateBookPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "replicatebookpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReplicateBookPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ReplicateBookPacket::dummy, //Idk how to make an empty packet/ping packet - so im just passing a dummy int. :(
            ReplicateBookPacket::new
    );

    @Override
    public Type<ReplicateBookPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ReplicateBookPacket> codec() {
        return STREAM_CODEC;
    }
}
