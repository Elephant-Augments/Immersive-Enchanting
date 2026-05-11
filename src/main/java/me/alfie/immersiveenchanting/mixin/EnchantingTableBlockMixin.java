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

/**
 * Mixin that overrides and extends the vanilla {@link EnchantingTableBlock} behavior.
 *
 * <p>Replaces the default menu provider with a custom {@link EnchantingTableMenu}
 * and injects additional server-side logic when the enchanting table is used.</p>
 *
 * <p>Also triggers bookshelf scanning after the GUI is opened to update
 * available enchantments based on the surrounding environment.</p>
 */
@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockMixin {

    /**
     * Replaces the vanilla enchanting table menu provider with the modded implementation.
     *
     * <p>This ensures that interacting with the enchanting table opens {@link EnchantingTableMenu}
     * instead of the vanilla enchanting screen.</p>
     *
     * <p>If the block entity is invalid, the return value is set to {@code null} to prevent
     * further processing.</p>
     *
     * @param state block state of the enchanting table
     * @param level world containing the block
     * @param pos block position
     * @param cir callback used to override the return value
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
     * Runs after the player successfully opens the enchanting table menu.
     *
     * <p>On the server, triggers a bookshelf scan to determine available enchantments
     * based on surrounding chiseled bookshelves and configuration rules.</p>
     *
     * <p>This is only executed for {@link ServerPlayer} instances on the server side.</p>
     *
     * @param state block state of the enchanting table
     * @param level world containing the block
     * @param pos interaction position
     * @param player interacting player
     * @param hitResult interaction hit result
     * @param cir callback for interaction result
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
