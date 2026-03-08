package me.alfie.immersiveenchanting.networking.packet.updatetoolslot;

import me.alfie.immersiveenchanting.networking.packet.NetworkPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record UpdateToolSlotPacket(int mode) implements NetworkPacket<UpdateToolSlotPacket> {
    public static final Type<UpdateToolSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "updatetoolslotpacket"));

    public enum MODE {
        TAKE,
        PLACE
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateToolSlotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateToolSlotPacket::mode,
            UpdateToolSlotPacket::new
    );

    @Override
    public Type<UpdateToolSlotPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, UpdateToolSlotPacket> codec() {
        return STREAM_CODEC;
    }
}
