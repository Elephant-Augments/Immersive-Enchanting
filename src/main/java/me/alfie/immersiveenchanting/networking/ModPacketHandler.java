package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.networking.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModPacketHandler {
    //Client to server: ModPackets.INSTANCE.sendToServer(new MyPacket(123));
    //Server to client: ModPackets.INSTANCE.send(
    //    PacketDistributor.PLAYER.with(() -> serverPlayer),
    //    new MyPacket(456)
    //);

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        INSTANCE.registerMessage(
                id++,
                GetBookshelfContentsPacket.class,
                GetBookshelfContentsPacket::encode,
                GetBookshelfContentsPacket::decode,
                GetBookshelfContentsPacket::handle
        );

        INSTANCE.registerMessage(
                id++,
                UnlockedEnchantmentsPacket.class,
                UnlockedEnchantmentsPacket::encode,
                UnlockedEnchantmentsPacket::decode,
                UnlockedEnchantmentsPacket::handle
        );

        INSTANCE.registerMessage(
                id++,
                EnchantItemPacket.class,
                EnchantItemPacket::encode,
                EnchantItemPacket::decode,
                EnchantItemPacket::handle
        );

        INSTANCE.registerMessage(
                id++,
                EnchantmentCostRegistrySyncPacket.class,
                EnchantmentCostRegistrySyncPacket::encode,
                EnchantmentCostRegistrySyncPacket::decode,
                EnchantmentCostRegistrySyncPacket::handle
        );

        INSTANCE.registerMessage(
                id++,
                UpdateToolSlotPacket.class,
                UpdateToolSlotPacket::encode,
                UpdateToolSlotPacket::decode,
                UpdateToolSlotPacket::handle
        );

        //... register more packets.
    }


}
