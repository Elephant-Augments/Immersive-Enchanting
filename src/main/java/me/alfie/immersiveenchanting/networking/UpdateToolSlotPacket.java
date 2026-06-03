package me.alfie.immersiveenchanting.networking;

import me.alfie.alfinolib.networking.NetworkPacket;
import me.alfie.alfinolib.networking.codec.CommonCodecs;
import me.alfie.alfinolib.networking.codec.StreamCodec;
import me.alfie.alfinolib.networking.codec.StreamCodecBuilder;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
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
public record UpdateToolSlotPacket(int mode) implements NetworkPacket<UpdateToolSlotPacket> {

    public enum Mode {
        TAKE,
        PLACE
    }

    public static final Type<@NotNull UpdateToolSlotPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ImmersiveEnchanting.MODID, "update_tool_slot"));
    @Override public Type<@NotNull UpdateToolSlotPacket> type() {return TYPE;}

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateToolSlotPacket> STREAM_CODEC =
            StreamCodecBuilder.<RegistryFriendlyByteBuf, UpdateToolSlotPacket>create()
                    .add(CommonCodecs.VAR_INT, UpdateToolSlotPacket::mode)
                    .build(UpdateToolSlotPacket::new);


    @Override
    public void exec(IPayloadContext context) {
        Player player = context.player();
        if(!(player.containerMenu instanceof EnchantingTableMenu menu)) return;

        Mode updateMode = Mode.values()[mode];
        Slot slot = menu.getToolSlot();
        ItemStack stack;

        if(updateMode == Mode.TAKE && menu.getCarried().isEmpty()) {
            stack = slot.getItem();
            menu.setCarried(stack.copyAndClear());
        } else if (updateMode == Mode.PLACE) {
            stack = menu.getCarried();
            slot.safeInsert(stack);
        }

        slot.setChanged();;
    }
}
