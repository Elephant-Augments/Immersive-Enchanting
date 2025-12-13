package me.alfie.immersiveenchanting.networking.packets;

import io.netty.buffer.ByteBuf;
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

public record EnchantItemPacket(ResourceKey<Enchantment> enchantment, int enchantmentLevel) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<EnchantItemPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "enchantmentpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantItemPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.ENCHANTMENT),
            EnchantItemPacket::enchantment,
            ByteBufCodecs.VAR_INT,
            EnchantItemPacket::enchantmentLevel,
            EnchantItemPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                EnchantItemPacket.TYPE,
                EnchantItemPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<EnchantItemPacket>(
                        null,
                        ServerPayloadHandler::onEnchantItem
                )
        );
    }
}
