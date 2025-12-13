package me.alfie.immersiveenchanting.networking.packets;

import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EnchantItemPacket {

    public final ResourceKey<Enchantment> enchantment;
    public final int enchantmentLevel;

    public EnchantItemPacket(ResourceKey<Enchantment> enchantment, int enchantmentLevel) {
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
    }

    public static void encode(EnchantItemPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceKey(packet.enchantment);
        buf.writeInt(packet.enchantmentLevel);
    }

    public static EnchantItemPacket decode(FriendlyByteBuf buf) {
        ResourceKey<Enchantment> enchantment = buf.readResourceKey(Registries.ENCHANTMENT);
        int enchantmentLevel = buf.readInt();
        return new EnchantItemPacket(enchantment, enchantmentLevel);
    }

    public static void handle(EnchantItemPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> {
                    ServerPayloadHandler.onEnchantItem(packet, contextSupplier.get());
                });
        contextSupplier.get().setPacketHandled(true);
    }

}
