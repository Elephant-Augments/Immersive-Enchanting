package me.alfie.immersiveenchanting.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface NetworkPacket<T extends CustomPacketPayload> extends CustomPacketPayload {

    CustomPacketPayload.Type<T> typeId();

    StreamCodec<RegistryFriendlyByteBuf, T> codec();

    @Override
    default Type<? extends CustomPacketPayload> type() {
        return typeId();
    }
}
