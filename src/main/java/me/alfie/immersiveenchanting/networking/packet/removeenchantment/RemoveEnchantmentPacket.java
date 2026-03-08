package me.alfie.immersiveenchanting.networking.packet.removeenchantment;

import me.alfie.immersiveenchanting.networking.packet.NetworkPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public record RemoveEnchantmentPacket(ResourceKey<Enchantment> enchantment, int enchantmentLevel) implements NetworkPacket<RemoveEnchantmentPacket> {
    public static final Type<RemoveEnchantmentPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "removeenchantmentpacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveEnchantmentPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.ENCHANTMENT),
            RemoveEnchantmentPacket::enchantment,
            ByteBufCodecs.VAR_INT,
            RemoveEnchantmentPacket::enchantmentLevel,
            RemoveEnchantmentPacket::new
    );

    @Override
    public Type<RemoveEnchantmentPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RemoveEnchantmentPacket> codec() {
        return STREAM_CODEC;
    }
}
