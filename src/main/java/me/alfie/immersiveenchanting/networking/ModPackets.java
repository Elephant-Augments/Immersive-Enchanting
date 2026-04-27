package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ModPackets {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static <T extends ModNetworkPacket> void register(Class<T> type, PacketCodec<T> codec) {
        INSTANCE.registerMessage(
                packetId++,
                type,
                codec::encode,
                codec::decode,
                ModPackets::handle
        );
    }

    public static <T extends ModNetworkPacket> void handle(T packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(
                () -> packet.exec(context.get())
        );
        context.get().setPacketHandled(true);
    }

    public static void register() {
        register(AvailableEnchantmentsPacket.class, AvailableEnchantmentsPacket.CODEC);
        register(EnchantPacket.class, EnchantPacket.CODEC);
        register(RemoveEnchantmentPacket.class, RemoveEnchantmentPacket.CODEC);
        register(ReplicatePacket.class, ReplicatePacket.CODEC);
        register(SyncClientDatapackManagerPacket.class, SyncClientDatapackManagerPacket.CODEC);
        register(TransmutePacket.class, TransmutePacket.CODEC);
        register(UpdateToolSlotPacket.class, UpdateToolSlotPacket.CODEC);
    }

    public static <T extends ModNetworkPacket> void toServer(T packet) {
        INSTANCE.sendToServer(packet);
    }

    public static <T extends ModNetworkPacket> void toClient(T packet, ServerPlayer serverPlayer) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
    }
}
