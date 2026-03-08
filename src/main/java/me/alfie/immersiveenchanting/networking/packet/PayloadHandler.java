package me.alfie.immersiveenchanting.networking.packet;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface PayloadHandler<T> {

    void execOnClient(final T packet, final IPayloadContext context);

    void execOnServer(final T packet, final IPayloadContext context);
}
