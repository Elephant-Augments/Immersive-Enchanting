package me.alfie.immersiveenchanting.networking.packet.unlockedenchantments;

import me.alfie.immersiveenchanting.networking.packet.NetworkPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

public record UnlockedEnchantmentsPacket(List<ResourceKey<Enchantment>> enchantments) implements NetworkPacket<UnlockedEnchantmentsPacket> {
    public static final Type<UnlockedEnchantmentsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("immersiveenchanting", "unlockedenchantmentspacket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnlockedEnchantmentsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ResourceKey.streamCodec(Registries.ENCHANTMENT)),
            UnlockedEnchantmentsPacket::enchantments,
            UnlockedEnchantmentsPacket::new
    );

    @Override
    public Type<UnlockedEnchantmentsPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, UnlockedEnchantmentsPacket> codec() {
        return STREAM_CODEC;
    }
}
