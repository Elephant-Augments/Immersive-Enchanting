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
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentTableBlock.class)
public abstract class EnchantingTableBlockMixin {

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
