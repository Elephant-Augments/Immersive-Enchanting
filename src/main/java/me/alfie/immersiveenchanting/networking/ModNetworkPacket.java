package me.alfie.immersiveenchanting.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public interface ModNetworkPacket<T extends CustomPacketPayload> extends CustomPacketPayload {

    StreamCodec<RegistryFriendlyByteBuf, T> codec();

    @Override
    default @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return typeId();
    }

    Type<@NotNull T> typeId();

    /**
     * Executes the packet logic on the receiving side.
     * Called on the game thread after the packet is received and decoded.
     *
     * @param packet  the decoded packet instance
     * @param context payload context providing the player and other execution helpers
     */
    void exec(T packet, IPayloadContext context);
}
