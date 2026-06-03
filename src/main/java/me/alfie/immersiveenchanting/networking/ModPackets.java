package me.alfie.immersiveenchanting.networking;

import me.alfie.alfinolib.networking.NetworkRegisterEvent;
import me.alfie.alfinolib.networking.Networking;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModPackets {

    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceKey<Enchantment>> ENCHANTMENT_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ResourceKey<Enchantment>>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, ResourceKey<Enchantment> enchantment) {
            buf.writeResourceKey(enchantment);
        }

        @Override
        public ResourceKey<Enchantment> decode(RegistryFriendlyByteBuf buf) {
            return buf.readResourceKey(Registries.ENCHANTMENT);
        }
    };


    public static void registerPackets(NetworkRegisterEvent event) {
        event.register(Networking.Side.CLIENT, AvailableEnchantmentsPacket.TYPE, AvailableEnchantmentsPacket.STREAM_CODEC);
        event.register(Networking.Side.SERVER, EnchantPacket.TYPE, EnchantPacket.STREAM_CODEC);
        event.register(Networking.Side.SERVER, RemoveEnchantmentPacket.TYPE, RemoveEnchantmentPacket.STREAM_CODEC);
        event.register(Networking.Side.SERVER, ReplicatePacket.TYPE, ReplicatePacket.STREAM_CODEC);
        event.register(Networking.Side.SERVER, TransmutePacket.TYPE, TransmutePacket.STREAM_CODEC);
        event.register(Networking.Side.SERVER, UpdateToolSlotPacket.TYPE, UpdateToolSlotPacket.STREAM_CODEC);
    }
}
