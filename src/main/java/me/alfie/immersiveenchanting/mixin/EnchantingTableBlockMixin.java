package me.alfie.immersiveenchanting.mixin;

import me.alfie.immersiveenchanting.block.ModBlocks;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.util.BookshelfChecker;
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
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
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

    /**
     * Filters which nearby blocks count as a valid bookshelf for the
     * enchanting-table particle effect (the floating glyphs that fly from the
     * shelves to the book).
     *
     * <p>Immersive Enchanting replaces vanilla's bookshelf-power system with
     * one based on chiseled bookshelves containing books. We restrict the
     * particle source to:</p>
     * <ul>
     *     <li>Chiseled bookshelves that contain at least one item, and</li>
     *     <li>The mod's own creative bookshelf block.</li>
     * </ul>
     *
     * <p>Plain wooden bookshelves are rejected so they don't emit particles.
     * Empty chiseled bookshelves are rejected too. We must fully handle the
     * decision here (no fall-through), because vanilla's own logic only
     * accepts {@code Blocks.BOOKSHELF} and would reject our chiseled / mod
     * bookshelves if we let it run.</p>
     */
    @Inject(method = "isValidBookShelf", at = @At("HEAD"), cancellable = true)
    private static void immersiveenchanting$isValidBookShelf(Level level, BlockPos pos, BlockPos offset, CallbackInfoReturnable<Boolean> cir) {
        BlockPos shelfPos = pos.offset(offset);
        BlockState shelfState = level.getBlockState(shelfPos);

        boolean shelfIsValid = false;

        // Mod's creative bookshelf always counts.
        if (shelfState.is(ModBlocks.CREATIVE_BOOKSHELF_BLOCK.get())) {
            shelfIsValid = true;
        } else {
            // Chiseled bookshelf with at least one item.
            BlockEntity be = level.getBlockEntity(shelfPos);
            if (be instanceof ChiseledBookShelfBlockEntity shelf) {
                for (int i = 0; i < shelf.getContainerSize(); i++) {
                    if (!shelf.getItem(i).isEmpty()) {
                        shelfIsValid = true;
                        break;
                    }
                }
            }
        }

        if (!shelfIsValid) {
            cir.setReturnValue(false);
            return;
        }

        // Replicate vanilla's "in-between space must be air" check so the
        // line-of-sight requirement still holds for our valid shelf types.
        // The in-between position is half-way between the table and the shelf,
        // at the shelf's Y coordinate.
        BlockPos inBetween = pos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2);
        cir.setReturnValue(level.getBlockState(inBetween).isAir());
    }


}
