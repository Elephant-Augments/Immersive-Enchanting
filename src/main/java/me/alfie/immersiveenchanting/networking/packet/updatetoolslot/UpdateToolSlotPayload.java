package me.alfie.immersiveenchanting.networking.packet.updatetoolslot;

import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.packet.PayloadHandler;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UpdateToolSlotPayload implements PayloadHandler<UpdateToolSlotPacket> {
    @Override //Empty
    public void execOnClient(UpdateToolSlotPacket packet, IPayloadContext context) {}

    @Override
    public void execOnServer(UpdateToolSlotPacket packet, IPayloadContext context) {
        int mode = packet.mode();

        Slot toolSlot = context.player().containerMenu.getSlot(EnchantingTableMenu.SLOTS.TOOL.ordinal());
        if(mode == UpdateToolSlotPacket.MODE.TAKE.ordinal()) {
            ItemStack itemStack = toolSlot.getItem();
            context.player().containerMenu.setCarried(itemStack.copyAndClear());
            toolSlot.setChanged();
        } else if (mode == UpdateToolSlotPacket.MODE.PLACE.ordinal()) {
            ItemStack carriedStack = context.player().containerMenu.getCarried();
            toolSlot.safeInsert(carriedStack);
            toolSlot.setChanged();
        }
    }
}
