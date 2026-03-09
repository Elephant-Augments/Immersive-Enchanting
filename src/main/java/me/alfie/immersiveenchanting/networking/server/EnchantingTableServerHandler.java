package me.alfie.immersiveenchanting.networking.server;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.block.CreativeBookshelf;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.lootmodifier.AncientBookLootModifier;
import me.alfie.immersiveenchanting.networking.packet.unlockedenchantments.UnlockedEnchantmentsPacket;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnchantingTableServerHandler {

    /**
     * Check nearby bookshelves for ancient books and send UnlockedEnchantmentsPacket to client.
     * @param tablePos
     * @param level
     * @param serverPlayer
     */
    public static void checkBookshelvesAndUpdateClient(BlockPos tablePos, Level level, ServerPlayer serverPlayer) {
        List<BlockEntity> bookshelves = getChiseledBookshelvesNearby(tablePos, level);

        List<ResourceKey<Enchantment>> unlockedEnchantments = new ArrayList<>();
        for (BlockEntity bookshelf : bookshelves) {
            List<ItemStack> books = getChiseledBookshelfContents(bookshelf);

            //Get books in bookshelf
            for (ItemStack book : books) {
                if (book.getItem() == ModItems.ANCIENT_BOOK.get()) {
                    ResourceKey<Enchantment> key = AncientBook.getStoredEnchantment(book, level);

                    if (key != null) {
                        unlockedEnchantments.add(key);
                    }
                }
            }
        }

        //Unlock all enchantments if creative bookshelf is near or if ancient book requirement disabled
        if(isCreativeBookshelfNearby(tablePos, level) || !ServerConfig.areAncientBooksRequired()) {
            //Clear unlockedEnchantments from the bookshelf search
            unlockedEnchantments.clear();

            //Add all enchantments that exist
            unlockedEnchantments = EnchantmentUtil.getAllEnchantments(level).stream()
                    .map(Holder.Reference::key)
                    .toList();
        }

        if(serverPlayer.containerMenu instanceof EnchantingTableMenu enchantingTableMenu) {
            Set<Holder<Enchantment>> enchantmentSet = new HashSet<>();

            for (ResourceKey<Enchantment> key : unlockedEnchantments) {
                ImmersiveEnchanting.getEnchantmentHolder(serverPlayer.registryAccess(), key)
                        .ifPresent(enchantmentSet::add);
            }

            enchantingTableMenu.setUnlockedEnchantments(enchantmentSet);
        }
        PacketDistributor.sendToPlayer(serverPlayer, new UnlockedEnchantmentsPacket(unlockedEnchantments));
    }

    /**
     * Returns all the itemIds contained in a chiseled bookshelf.
     * @param shelf
     * @return
     */
    private static List<ItemStack> getChiseledBookshelfContents(BlockEntity shelf) {
        List<ItemStack> contents = new ArrayList<>();
        if (shelf instanceof ChiseledBookShelfBlockEntity) {
            for (int i = 0; i < 6; i++) {
                ItemStack stack = ((ChiseledBookShelfBlockEntity) shelf).getItem(i);
                contents.add(stack);
            }

        }
        return contents;
    }

    /**
     * Returns a list of all the bookshelves in a 5x5 radius of a coordinate.
     * Same positions as the vanilla enchanting table searches for.
     * @param pos
     * @param level
     * @return
     */
    private static List<BlockEntity> getChiseledBookshelvesNearby(BlockPos pos, Level level) {
        List<BlockEntity> blockEntities = new ArrayList<>();

        int radiusX = ServerConfig.getBookshelfSearchX();
        int radiusY = ServerConfig.getBookshelfSearchY();
        int radiusZ = ServerConfig.getBookshelfSearchZ();

        for (int dy = 0; dy < radiusY; dy++) {

            for (int rX = 2; rX <= radiusX; rX++) {
                for (int rZ = 2; rZ <= radiusZ; rZ++) {

                    for (int dx = -rX; dx <= rX; dx++) {
                        for (int dz = -rZ; dz <= rZ; dz++) {

                            // Only boundary of this ring
                            if (Math.abs(dx) != rX && Math.abs(dz) != rZ) continue;

                            BlockPos checkPos = pos.offset(dx, dy, dz);
                            BlockEntity blockEntity = level.getBlockEntity(checkPos);

                            if (blockEntity instanceof ChiseledBookShelfBlockEntity) {
                                blockEntities.add(blockEntity);
                            }
                        }
                    }
                }
            }
        }

        return blockEntities;
    }

    /**
     * Returns true if a creative bookshelf is within the 5x5 ring.
     * @param pos
     * @param level
     * @return
     */
    private static boolean isCreativeBookshelfNearby(BlockPos pos, Level level) {
        for (int dy = 0; dy <= 1; dy++) { // check table level and level above
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    // Skip inner 3x3 square; only outer ring
                    if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;

                    BlockPos checkPos = pos.offset(dx, dy, dz);

                    if (level.getBlockState(checkPos).getBlock() instanceof CreativeBookshelf) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

