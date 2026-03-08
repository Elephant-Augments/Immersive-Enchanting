package me.alfie.immersiveenchanting.networking.packet.enchantitem;

import me.alfie.immersiveenchanting.networking.packet.NetworkPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public record EnchantItemPacket(ResourceKey<Enchantment> enchantment, int enchantmentLevel) implements NetworkPacket<EnchantItemPacket> {
    public static final CustomPacketPayload.Type<EnchantItemPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "enchantmentpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnchantItemPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.ENCHANTMENT),
            EnchantItemPacket::enchantment,
            ByteBufCodecs.VAR_INT,
            EnchantItemPacket::enchantmentLevel,
            EnchantItemPacket::new
    );


    @Override
    public Type<EnchantItemPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EnchantItemPacket> codec() {
        return STREAM_CODEC;
    }
}
