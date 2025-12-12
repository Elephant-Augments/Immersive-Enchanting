package me.alfie.immersiveenchanting.networking.packets;

import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EnchantItemPacket {

    public final String enchantmentResourceId;
    public final int enchantmentLevel;

    public EnchantItemPacket(String enchantmentResourceId, int enchantmentLevel) {
        this.enchantmentResourceId = enchantmentResourceId;
        this.enchantmentLevel = enchantmentLevel;
    }

    public static void encode(EnchantItemPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.enchantmentResourceId);
        buf.writeInt(packet.enchantmentLevel);
    }

    public static EnchantItemPacket decode(FriendlyByteBuf buf) {
        String enchantmentResourceId = buf.readUtf();
        int enchantmentLevel = buf.readInt();
        return new EnchantItemPacket(enchantmentResourceId, enchantmentLevel);
    }

    public static void handle(EnchantItemPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> {
                    ServerPayloadHandler.onEnchantItem(packet, contextSupplier.get());
                });
        contextSupplier.get().setPacketHandled(true);
    }

}
