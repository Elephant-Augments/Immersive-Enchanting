package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateToolSlotPacket(int mode) implements ModNetworkPacket {


    public enum Mode {
        TAKE,
        PLACE
    }

    public static final PacketCodec<UpdateToolSlotPacket> CODEC = new PacketCodec<UpdateToolSlotPacket>() {
        @Override
        public void encode(UpdateToolSlotPacket packet, FriendlyByteBuf buf) {
            buf.writeInt(packet.mode());
        }

        @Override
        public UpdateToolSlotPacket decode(FriendlyByteBuf buf) {
            return new UpdateToolSlotPacket(buf.readInt());
        }
    };

    @Override
    public void exec(NetworkEvent.Context context) {
        if(!context.getDirection().getReceptionSide().isServer()) return;

        Mode mode = Mode.values()[mode()];

        EnchantingTableMenu menu = (EnchantingTableMenu) context.getSender().containerMenu;
        Slot slot = menu.getToolSlot();
        ItemStack stack;

        if(mode == Mode.TAKE && menu.getCarried().isEmpty()) {
            stack = slot.getItem();
            menu.setCarried(stack.copyAndClear());
        } else if (mode == Mode.PLACE) {
            stack = menu.getCarried();
            slot.safeInsert(stack);
        }
        slot.setChanged();
    }

}
