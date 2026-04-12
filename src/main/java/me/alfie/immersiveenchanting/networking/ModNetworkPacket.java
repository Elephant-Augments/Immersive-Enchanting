package me.alfie.immersiveenchanting.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public interface ModNetworkPacket<T extends CustomPacketPayload> extends CustomPacketPayload {

    Type<@NotNull T> typeId();

    StreamCodec<RegistryFriendlyByteBuf, T> codec();

    @Override
    default @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return typeId();
    }

    void exec(T packet, IPayloadContext context);
}
