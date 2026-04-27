package me.alfie.immersiveenchanting.networking;

import net.minecraft.network.FriendlyByteBuf;

public interface PacketCodec<T extends ModNetworkPacket> {

    void encode(T packet, FriendlyByteBuf buf);

    T decode(FriendlyByteBuf buf);
}
