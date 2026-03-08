package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import me.alfie.immersiveenchanting.networking.packet.enchantitem.EnchantItemPacket;
import me.alfie.immersiveenchanting.networking.packet.enchantitem.EnchantItemPayload;
import me.alfie.immersiveenchanting.networking.packet.enchantmentcostregistrysync.EnchantmentCostRegistrySyncPacket;
import me.alfie.immersiveenchanting.networking.packet.enchantmentcostregistrysync.EnchantmentCostRegistrySyncPayload;
import me.alfie.immersiveenchanting.networking.packet.removeenchantment.RemoveEnchantmentPacket;
import me.alfie.immersiveenchanting.networking.packet.removeenchantment.RemoveEnchantmentPayload;
import me.alfie.immersiveenchanting.networking.packet.replicatebookpacket.ReplicateBookPacket;
import me.alfie.immersiveenchanting.networking.packet.replicatebookpacket.ReplicateBookPayload;
import me.alfie.immersiveenchanting.networking.packet.transmutebookpacket.TransmuteBookPacket;
import me.alfie.immersiveenchanting.networking.packet.transmutebookpacket.TransmuteBookPayload;
import me.alfie.immersiveenchanting.networking.packet.unlockedenchantments.UnlockedEnchantmentsPacket;
import me.alfie.immersiveenchanting.networking.packet.unlockedenchantments.UnlockedEnchantmentsPayload;
import me.alfie.immersiveenchanting.networking.packet.updatetoolslot.UpdateToolSlotPacket;
import me.alfie.immersiveenchanting.networking.packet.updatetoolslot.UpdateToolSlotPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {


    public static void register(RegisterPayloadHandlersEvent event) {
        register(event,
                EnchantItemPacket.TYPE,
                EnchantItemPacket.STREAM_CODEC,
                new EnchantItemPayload());

        register(event,
                EnchantmentCostRegistrySyncPacket.TYPE,
                EnchantmentCostRegistrySyncPacket.STREAM_CODEC,
                new EnchantmentCostRegistrySyncPayload());

        register(event,
                RemoveEnchantmentPacket.TYPE,
                RemoveEnchantmentPacket.STREAM_CODEC,
                new RemoveEnchantmentPayload());

        register(event,
                ReplicateBookPacket.TYPE,
                ReplicateBookPacket.STREAM_CODEC,
                new ReplicateBookPayload());

        register(event,
                TransmuteBookPacket.TYPE,
                TransmuteBookPacket.STREAM_CODEC,
                new TransmuteBookPayload());

        register(event,
                UnlockedEnchantmentsPacket.TYPE,
                UnlockedEnchantmentsPacket.STREAM_CODEC,
                new UnlockedEnchantmentsPayload());

        register(event,
                UpdateToolSlotPacket.TYPE,
                UpdateToolSlotPacket.STREAM_CODEC,
                new UpdateToolSlotPayload());
    }

    private static <T extends CustomPacketPayload> void register(
            RegisterPayloadHandlersEvent event,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec,
            PayloadHandler<T> handler
    ) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playBidirectional(
                type,
                codec,
                new DirectionalPayloadHandler<>(
                        handler::execOnClient,
                        handler::execOnServer
                )
        );
    }
}
