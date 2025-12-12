package me.alfie.immersiveenchanting.networking.packets;

import me.alfie.immersiveenchanting.networking.ServerPayloadHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GetBookshelfContentsPacket {
    public final int blockPosX;
    public final int blockPosY;
    public final int blockPosZ;

    public GetBookshelfContentsPacket(int blockPosX, int blockPosY, int blockPosZ) {
        this.blockPosX = blockPosX;
        this.blockPosY = blockPosY;
        this.blockPosZ = blockPosZ;
    }

    public static void encode(GetBookshelfContentsPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.blockPosX);
        buf.writeInt(packet.blockPosY);
        buf.writeInt(packet.blockPosZ);
    }

    public static GetBookshelfContentsPacket decode(FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        return new GetBookshelfContentsPacket(x, y, z);
    }

    public static void handle(GetBookshelfContentsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(
                () -> {
                    ServerPayloadHandler.onGetBookshelfContentsPacket(packet, contextSupplier.get());
                });
        contextSupplier.get().setPacketHandled(true);
    }

}
