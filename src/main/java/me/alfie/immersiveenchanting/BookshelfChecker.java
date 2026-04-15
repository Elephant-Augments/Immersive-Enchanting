package me.alfie.immersiveenchanting;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentUtil;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.networking.AvailableEnchantmentsPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class BookshelfChecker {
    public static void checkBookshelves(BlockPos blockPos, Level level, ServerPlayer serverPlayer) {
        List<Holder<Enchantment>> availableEnchantments = getEnchantmentsInBookshelves(blockPos, level);

        ImmersiveEnchanting.LOGGER.debug("Sending available enchantments packet...");
        PacketDistributor.sendToPlayer(serverPlayer, new AvailableEnchantmentsPacket(availableEnchantments));
    }

    public static List<Holder<Enchantment>> getEnchantmentsInBookshelves(BlockPos blockPos, Level level) {
        List<ChiseledBookShelfBlockEntity> bookshelves = getNearbyBookshelves(blockPos, level);
        List<Holder<Enchantment>> result = new ArrayList<>();

        for(ChiseledBookShelfBlockEntity bookshelf : bookshelves) {
            List<ItemStack> books = getBooks(bookshelf);

            for(ItemStack stack : books) {
                if(stack.getItem() == ModItems.ANCIENT_BOOK.get()) {
                    Holder<Enchantment> enchantmentHolder = EnchantmentUtil.getStoredEnchantment(stack);
                    if(enchantmentHolder != null) result.add(enchantmentHolder);
                }
            }
        }

        return result;
    }

    /**
     * Searches for nearby chiseled bookshelves within a predefined radius.
     *
     * <p>The scan forms a hollow rectangular prism around the enchanting table,
     * similar to vanilla enchanting mechanics.</p>
     *
     * @return A list of nearby {@link ChiseledBookShelfBlockEntity} instances
     */
    private static List<ChiseledBookShelfBlockEntity> getNearbyBookshelves(BlockPos blockPos, Level level) {
        List<ChiseledBookShelfBlockEntity> result = new ArrayList<>();

        int radiusX = 2;
        int radiusY = 3;
        int radiusZ = 2;

        for (int dy = 0; dy < radiusY; dy++) {
            for (int rX = 2; rX <= radiusX; rX++) {
                for (int rZ = 2; rZ <= radiusZ; rZ++) {
                    for (int dx = -rX; dx <= rX; dx++) {
                        for (int dz = -rZ; dz <= rZ; dz++) {
                            if (Math.abs(dx) != rX && Math.abs(dz) != rZ) continue;

                            BlockPos checkPos = blockPos.offset(dx, dy, dz);
                            BlockEntity blockEntity = level.getBlockEntity(checkPos);

                            if (blockEntity instanceof ChiseledBookShelfBlockEntity chiseledBookshelf) result.add(chiseledBookshelf);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Retrieves all item stacks stored within a chiseled bookshelf.
     *
     * @param bookshelf The bookshelf block entity
     * @return A list of all 6 item slots contained in the bookshelf
     */
    private static List<ItemStack> getBooks(ChiseledBookShelfBlockEntity bookshelf) {
        List<ItemStack> result = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            result.add(bookshelf.getItem(i));
        }

        return result;
    }
}
