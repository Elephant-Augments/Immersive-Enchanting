package me.alfie.immersiveenchanting.networking.packets;

import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.networking.ClientPayloadHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UnlockedEnchantmentsPacket {

    public final List<ResourceKey<Enchantment>> enchantments;

    public UnlockedEnchantmentsPacket( List<ResourceKey<Enchantment>> enchantments) {
        this.enchantments = enchantments;
    }

    public static void encode(UnlockedEnchantmentsPacket packet, FriendlyByteBuf buf) {
        buf.writeCollection(packet.enchantments, FriendlyByteBuf::writeResourceKey);
    }

    public static UnlockedEnchantmentsPacket decode(FriendlyByteBuf buf) {
        List<ResourceKey<Enchantment>> enchantments = buf.readList(b -> b.readResourceKey(Registries.ENCHANTMENT));
        return new UnlockedEnchantmentsPacket(enchantments);
    }

    public static void handle(UnlockedEnchantmentsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> {
                    ClientPayloadHandler.onUnlockedEnchantments(packet, contextSupplier.get());
                });
        contextSupplier.get().setPacketHandled(true);
    }
}
