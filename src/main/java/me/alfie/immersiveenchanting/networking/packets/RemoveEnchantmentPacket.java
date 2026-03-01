package me.alfie.immersiveenchanting.networking.packets;

import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public record RemoveEnchantmentPacket(ResourceKey<Enchantment> enchantment, int enchantmentLevel) implements CustomPacketPayload {
    public static final Type<RemoveEnchantmentPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "removeenchantmentpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveEnchantmentPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.ENCHANTMENT),
            RemoveEnchantmentPacket::enchantment,
            ByteBufCodecs.VAR_INT,
            RemoveEnchantmentPacket::enchantmentLevel,
            RemoveEnchantmentPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                RemoveEnchantmentPacket.TYPE,
                RemoveEnchantmentPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<RemoveEnchantmentPacket>(
                        null,
                        ServerPayloadHandler::onRemoveEnchantment
                )
        );
    }
}
