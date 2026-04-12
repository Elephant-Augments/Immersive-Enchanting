package me.alfie.immersiveenchanting.networking;

import io.netty.buffer.ByteBuf;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateToolSlotPacket(int mode) implements ModNetworkPacket<UpdateToolSlotPacket> {

    public enum Mode {
        TAKE,
        PLACE
    }

    public static final Type<@NotNull UpdateToolSlotPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "update_tool_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateToolSlotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateToolSlotPacket::mode,
            UpdateToolSlotPacket::new
    );

    @Override
    public Type<@NotNull UpdateToolSlotPacket> typeId() {
        return TYPE;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, UpdateToolSlotPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public void exec(UpdateToolSlotPacket packet, IPayloadContext context) {
        Mode mode = Mode.values()[packet.mode()];

        EnchantingTableMenu menu = (EnchantingTableMenu) context.player().containerMenu;
        Slot slot = menu.getToolSlot();
        ItemStack stack;

        if(mode == Mode.TAKE && menu.getCarried().isEmpty()) {
            stack = slot.getItem();
            menu.setCarried(stack.copyAndClear());
        } else if (mode == Mode.PLACE) {
            stack = menu.getCarried();
            slot.safeInsert(stack);
        }
        slot.setChanged();;
    }


}
