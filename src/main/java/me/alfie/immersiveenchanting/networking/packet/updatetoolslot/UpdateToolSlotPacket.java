package me.alfie.immersiveenchanting.networking.packet.updatetoolslot;

import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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
                () -> exec(packet, contextSupplier.get()));
        contextSupplier.get().setPacketHandled(true);
    }

    public static void exec(UpdateToolSlotPacket packet, NetworkEvent.Context context) {
        int mode = packet.mode;

        Slot toolSlot = context.getSender().containerMenu.getSlot(EnchantingTableMenu.SLOTS.TOOL.ordinal());
        if(mode == UpdateToolSlotPacket.MODE.TAKE.ordinal()) {
            ItemStack itemStack = toolSlot.getItem();
            context.getSender().containerMenu.setCarried(itemStack.copyAndClear());
            toolSlot.setChanged();
        } else if (mode == UpdateToolSlotPacket.MODE.PLACE.ordinal()) {
            ItemStack carriedStack = context.getSender().containerMenu.getCarried();
            toolSlot.safeInsert(carriedStack);
            toolSlot.setChanged();
        }
    }

    public enum MODE {
        TAKE,
        PLACE
    }
}
