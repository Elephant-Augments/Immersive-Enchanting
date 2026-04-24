package me.alfie.immersiveenchanting.mixin;

import me.alfie.immersiveenchanting.util.BookshelfChecker;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin {

    /**
     * Replaces the vanilla enchanting table MenuProvider with a custom implementation
     * that opens a modded EnchantingTableMenu.
     *
     * This injection runs at the start of getMenuProvider and overrides the returned
     * MenuProvider to ensure that interacting with the enchanting table opens the
     * custom GUI instead of the vanilla enchanting screen.
     *
     * If the block entity at the given position is not a valid EnchantingTableBlockEntityMixin,
     * the return value is explicitly set to null to prevent further processing.
     *
     * @param state the current block state of the enchanting table
     * @param level the world containing the block
     * @param pos   the position of the block in the world
     * @param cir   callback used to override the return value of the method
     */
    @Inject(
            method = "getMenuProvider(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/MenuProvider;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void immersiveenchanting$getMenuProvider(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<MenuProvider> cir) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof EnchantingTableBlockEntity enchantingTable) {
            Component title = enchantingTable.getDisplayName();

            cir.setReturnValue(new SimpleMenuProvider(
                    (containerId, inventory, player) ->
                            new EnchantingTableMenu(containerId, inventory, level, pos),
                    title
            ));

        } else {
            cir.setReturnValue(null);
        }
    }

    /**
     * Executes additional logic after the player opens the enchanting table menu.
     *
     * This injection runs immediately after the vanilla call to Player.openMenu,
     * allowing post-processing once the GUI has been successfully opened.
     *
     * On the server side, this triggers a bookshelf validation check around the
     * enchanting table to determine the current enchanting power setup.
     *
     * This logic only runs on the server and only for ServerPlayer instances
     * to avoid unnecessary client-side execution.
     *
     * @param state     the block state of the enchanting table
     * @param level     the world containing the block
     * @param pos       the position of the block being interacted with
     * @param player    the player interacting with the block
     * @param hitResult the exact hit result of the interaction
     * @param cir       callback returning the interaction result
     */
    @Inject(
            method = "useWithoutItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;",
                    shift = At.Shift.AFTER
            )
    )
    private void immersiveenchanting$afterOpenMenu(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BookshelfChecker.checkBookshelves(pos, level, serverPlayer);
        }


    }


}
