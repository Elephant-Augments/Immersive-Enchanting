package me.alfie.immersiveenchanting.networking;

import net.minecraft.network.FriendlyByteBuf;

public interface StreamCodec<F> {
    void encode(FriendlyByteBuf buf, F value);

    F decode(FriendlyByteBuf buf);
}
