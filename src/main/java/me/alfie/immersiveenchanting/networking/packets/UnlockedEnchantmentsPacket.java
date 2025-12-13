package me.alfie.immersiveenchanting.networking.packets;

import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.networking.ClientPayloadHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

public record UnlockedEnchantmentsPacket(List<String> enchantments) implements CustomPacketPayload {
    public static final Type<UnlockedEnchantmentsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "unlockedenchantmentpacket"));

    public static final StreamCodec<ByteBuf, UnlockedEnchantmentsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.STRING_UTF8),
            UnlockedEnchantmentsPacket::enchantments,
            UnlockedEnchantmentsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                UnlockedEnchantmentsPacket.TYPE,
                UnlockedEnchantmentsPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<UnlockedEnchantmentsPacket>(
                        ClientPayloadHandler::onUnlockedEnchantments,
                        null
                )
        );
    }
}
