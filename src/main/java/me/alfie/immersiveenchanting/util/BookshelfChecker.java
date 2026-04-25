package me.alfie.immersiveenchanting.util;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
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

/**
 * Utility for scanning and resolving enchantments from nearby bookshelves.
 *
 * <p>This class is responsible for determining which enchantments are
 * available to the enchanting system based on the surrounding environment,
 * including chiseled bookshelves and special creative blocks.</p>
 *
 * <p>It also handles server-side syncing of available enchantments to players.</p>
 */
public class BookshelfChecker {

    /**
     * Scans nearby bookshelves and sends available enchantments to the player.
     *
     * @param blockPos center position of the enchanting table
     * @param level world level
     * @param serverPlayer player to send results to
     */
    public static void checkBookshelves(BlockPos blockPos, Level level, ServerPlayer serverPlayer) {
        List<Holder<Enchantment>> availableEnchantments = getEnchantmentsInBookshelves(blockPos, level);

        PacketDistributor.sendToPlayer(serverPlayer, new AvailableEnchantmentsPacket(availableEnchantments));
    }

    /**
     * Gets all enchantments available from nearby bookshelves.
     *
     * <p>If a creative bookshelf is present, or ancient books are not required,
     * all enchantments are returned.</p>
     *
     * @param blockPos center position of the scan
     * @param level world level
     * @return list of available enchantments
     */
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
     * Scans for chiseled bookshelves within a configured rectangular ring volume.
     *
     * <p>The scan mimics a hollow prism around the enchanting table rather than a full cube.</p>
     *
     * @param pos center position
     * @param level world level
     * @return nearby chiseled bookshelf block entities
     */
    private static List<ChiseledBookShelfBlockEntity> getNearbyBookshelves(BlockPos pos, Level level) {
        List<ChiseledBookShelfBlockEntity> result = new ArrayList<>();

        forEachRingPos(pos,
                ServerConfig.getBookshelfSearchRadius().x(),
                ServerConfig.getBookshelfSearchRadius().y(),
                ServerConfig.getBookshelfSearchRadius().z(),
                checkPos -> {
            BlockEntity be = level.getBlockEntity(checkPos);

            if (be instanceof ChiseledBookShelfBlockEntity shelf) {
                result.add(shelf);
            }
        });

        return result;
    }

    /**
     * Retrieves all item stacks stored inside a chiseled bookshelf.
     *
     * @param bookshelf bookshelf block entity
     * @return list of all 6 item slots
     */
    private static List<ItemStack> getBooks(ChiseledBookShelfBlockEntity bookshelf) {
        List<ItemStack> result = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            result.add(bookshelf.getItem(i));
        }

        return result;
    }

    /**
     * Iterates over all positions in a hollow ring-shaped prism and applies a consumer.
     *
     * @param center center position
     * @param radiusX horizontal X radius
     * @param radiusY vertical height
     * @param radiusZ horizontal Z radius
     * @param consumer position consumer
     */
    private static void forEachRingPos(BlockPos center, int radiusX, int radiusY, int radiusZ, Consumer<BlockPos> consumer) {
        for (int dy = 0; dy < radiusY; dy++) {
            for (int dx = -radiusX; dx <= radiusX; dx++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    if (Math.abs(dx) < radiusX && Math.abs(dz) < radiusZ) continue;

                    consumer.accept(center.offset(dx, dy, dz));
                }
            }
        }
    }

    /**
     * Checks if any block position in a hollow ring matches a condition.
     *
     * @param center center position
     * @param radiusX X radius
     * @param radiusY Y height
     * @param radiusZ Z radius
     * @param predicate condition to test positions
     * @return true if any position matches
     */
    private static boolean anyInRing(BlockPos center, int radiusX, int radiusY, int radiusZ, Predicate<BlockPos> predicate) {
        for (int dy = 0; dy < radiusY; dy++) {
            for (int dx = -radiusX; dx <= radiusX; dx++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {

                    if (Math.abs(dx) < radiusX && Math.abs(dz) < radiusZ) continue;

                    if (predicate.test(center.offset(dx, dy, dz))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if a creative bookshelf exists within range.
     *
     * @param pos center position
     * @param level world level
     * @return true if a creative bookshelf is nearby
     */
    private static boolean isCreativeBookshelfNearby(BlockPos pos, Level level) {
        return anyInRing(pos,
                ServerConfig.getBookshelfSearchRadius().x(),
                ServerConfig.getBookshelfSearchRadius().y(),
                ServerConfig.getBookshelfSearchRadius().z(),
                checkPos ->
                level.getBlockState(checkPos).getBlock()
                        .equals(ModBlocks.CREATIVE_BOOKSHELF_BLOCK.get())
        );
    }
}
