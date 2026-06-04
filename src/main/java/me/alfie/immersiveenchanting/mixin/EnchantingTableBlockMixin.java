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

    @Inject(method = "getMenuProvider(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/MenuProvider;",
            at = @At("HEAD"), cancellable = true)
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

    @Inject(method = "useWithoutItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;", shift = At.Shift.AFTER))
    private void immersiveenchanting$afterOpenMenu(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BookshelfChecker.checkBookshelves(pos, level, serverPlayer);
        }
    }

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
