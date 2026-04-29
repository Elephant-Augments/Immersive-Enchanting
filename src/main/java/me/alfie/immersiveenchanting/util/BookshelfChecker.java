package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.block.ModBlocks;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BookshelfChecker {
    public static void checkBookshelves(BlockPos blockPos, Level level, ServerPlayer serverPlayer) {
        List<Holder<Enchantment>> availableEnchantments = getEnchantmentsInBookshelves(blockPos, level);

        PacketDistributor.sendToPlayer(serverPlayer, new AvailableEnchantmentsPacket(availableEnchantments));
    }

    public static List<Holder<Enchantment>> getEnchantmentsInBookshelves(BlockPos blockPos, Level level) {
        if(isCreativeBookshelfNearby(blockPos, level)) return CostRegistry.server().getAllEnchantmentHolders();
        if(!ServerConfig.areAncientBooksRequired()) return CostRegistry.server().getAllEnchantmentHolders();

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
    private static List<ChiseledBookShelfBlockEntity> getNearbyBookshelves(BlockPos pos, Level level) {
        List<ChiseledBookShelfBlockEntity> result = new ArrayList<>();

        forEachRingPos(pos,
                2, ServerConfig.getBookshelfSearchRadius().x(),
                0, ServerConfig.getBookshelfSearchRadius().y(),
                2, ServerConfig.getBookshelfSearchRadius().z(),
                checkPos -> {
            BlockEntity be = level.getBlockEntity(checkPos);

            if (be instanceof ChiseledBookShelfBlockEntity shelf) {
                result.add(shelf);
            }
        });

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

    private static void forEachRingPos(
            BlockPos center,
            int minRadiusX, int maxRadiusX,
            int minRadiusY, int maxRadiusY,
            int minRadiusZ, int maxRadiusZ,
            Consumer<BlockPos> consumer
    ) {
        for (int dy = minRadiusY; dy <= maxRadiusY; dy++) {
            for (int dx = -maxRadiusX; dx <= maxRadiusX; dx++) {
                for (int dz = -maxRadiusZ; dz <= maxRadiusZ; dz++) {

                    int absX = Math.abs(dx);
                    int absZ = Math.abs(dz);

                    //skip inside inner ring
                    if (absX < minRadiusX && absZ < minRadiusZ) continue;

                    //skip max bounds
                    if (absX > maxRadiusX || absZ > maxRadiusZ) continue;

                    consumer.accept(center.offset(dx, dy, dz));
                }
            }
        }
    }

    private static boolean anyInRing(
            BlockPos center,
            int minRadiusX, int maxRadiusX,
            int minRadiusY, int maxRadiusY,
            int minRadiusZ, int maxRadiusZ,
            Predicate<BlockPos> predicate
    ) {
        for (int dy = minRadiusY; dy <= maxRadiusY-1; dy++) {
            for (int dx = -maxRadiusX; dx <= maxRadiusX; dx++) {
                for (int dz = -maxRadiusZ; dz <= maxRadiusZ; dz++) {

                    int absX = Math.abs(dx);
                    int absZ = Math.abs(dz);

                    //skip inside inner ring
                    if (absX < minRadiusX && absZ < minRadiusZ) continue;

                    //skip max bounds
                    if (absX > maxRadiusX || absZ > maxRadiusZ) continue;

                    if (predicate.test(center.offset(dx, dy, dz))) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns true if a creative bookshelf is within the 5x5 ring.
     * @param pos
     * @param level
     * @return
     */
    private static boolean isCreativeBookshelfNearby(BlockPos pos, Level level) {
        return anyInRing(pos,
                2, ServerConfig.getBookshelfSearchRadius().x(),
                0, ServerConfig.getBookshelfSearchRadius().y(),
                2, ServerConfig.getBookshelfSearchRadius().z(),
                checkPos ->
                level.getBlockState(checkPos).getBlock()
                        .equals(ModBlocks.CREATIVE_BOOKSHELF_BLOCK.get())
        );
    }
}
