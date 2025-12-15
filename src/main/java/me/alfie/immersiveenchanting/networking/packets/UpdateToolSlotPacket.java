package me.alfie.immersiveenchanting.networking.packets;

import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateToolSlotPacket {

    public final int mode;

    public UpdateToolSlotPacket(int mode) {
        this.mode = mode;
    }

    public static void encode(UpdateToolSlotPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.mode);
    }

    public static UpdateToolSlotPacket decode(FriendlyByteBuf buf) {
        int mode = buf.readInt();
        return new UpdateToolSlotPacket(mode);
    }

    public static void handle(UpdateToolSlotPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> {
                    ServerPayloadHandler.onUpdateSlotPacket(packet, contextSupplier.get());
                });
        contextSupplier.get().setPacketHandled(true);
    }

    public enum MODE {
        TAKE,
        PLACE
    }
}
