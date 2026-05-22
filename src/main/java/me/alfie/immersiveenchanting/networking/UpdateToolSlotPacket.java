package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Client-to-server packet for synchronizing tool slot interactions in the enchanting table.
 *
 * <p>Handles manual item transfer between the tool slot and the player cursor,
 * supporting both taking and placing items.</p>
 *
 * <p>Used to keep custom enchanting UI behavior in sync with server-side inventory state
 * when standard container interactions are bypassed or extended.</p>
 */
public record UpdateToolSlotPacket(int mode) implements ModNetworkPacket<UpdateToolSlotPacket> {

    public static final Type<@NotNull UpdateToolSlotPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "update_tool_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateToolSlotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UpdateToolSlotPacket::mode,
            UpdateToolSlotPacket::new
    );

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, UpdateToolSlotPacket> codec() {
        return STREAM_CODEC;
    }

    @Override
    public Type<@NotNull UpdateToolSlotPacket> typeId() {
        return TYPE;
    }

    @Override
    public void exec(UpdateToolSlotPacket packet, IPayloadContext context) {
        Mode mode = Mode.values()[packet.mode()];

        EnchantingTableMenu menu = (EnchantingTableMenu) context.player().containerMenu;
        Slot slot = menu.getToolSlot();
        ItemStack stack;

        if (mode == Mode.TAKE && menu.getCarried().isEmpty()) {
            stack = slot.getItem();
            menu.setCarried(stack.copyAndClear());
        } else if (mode == Mode.PLACE) {
            stack = menu.getCarried();
            slot.safeInsert(stack);
        }
        slot.setChanged();
    }

    public enum Mode {
        TAKE,
        PLACE
    }


}
