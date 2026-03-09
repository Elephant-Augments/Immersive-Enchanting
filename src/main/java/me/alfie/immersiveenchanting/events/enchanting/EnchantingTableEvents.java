package me.alfie.immersiveenchanting.events.enchanting;

import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.networking.server.EnchantingTableServerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class EnchantingTableEvents {

    /**
     * Opens the custom EnchantingTableMenu/Screen instead of the default screen.
     * @param event
     */
    @SubscribeEvent
    public static void onEnchantingTableInteract(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        if(level.isClientSide) return;
        if(!(player instanceof ServerPlayer serverPlayer)) return;

        if (level.getBlockState(pos).getBlock() == Blocks.ENCHANTING_TABLE) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            // Open the menu
            serverPlayer.openMenu(
                    new SimpleMenuProvider((id, inv, p) -> new EnchantingTableMenu(id, inv, level, pos),
                            Component.literal("Enchanting Table")), buf -> buf.writeBlockPos(pos));
            EnchantingTableServerHandler.checkBookshelvesAndUpdateClient(pos, player.level(), serverPlayer);
        }
    }
}
