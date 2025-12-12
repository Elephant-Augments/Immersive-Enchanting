package me.alfie.immersiveenchanting.networking.packets;

import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.networking.ClientPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UnlockedEnchantmentsPacket {

    public final List<String> enchantments;

    public UnlockedEnchantmentsPacket(List<String> enchantments) {
        this.enchantments = enchantments;
    }

    public static void encode(UnlockedEnchantmentsPacket packet, FriendlyByteBuf buf) {
        buf.writeCollection(packet.enchantments, (b, str) -> b.writeUtf(str));
    }

    public static UnlockedEnchantmentsPacket decode(FriendlyByteBuf buf) {
        List<String> enchantments = buf.readList(b -> b.readUtf());
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
