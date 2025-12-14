package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.block.CreativeBookshelf;
import me.alfie.immersiveenchanting.compat.Compat;
import me.alfie.immersiveenchanting.datacomponents.EnchantmentDataComponent;
import me.alfie.immersiveenchanting.datacomponents.ModDataComponents;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.networking.packets.EnchantItemPacket;
import me.alfie.immersiveenchanting.networking.packets.GetBookshelfContentsPacket;
import me.alfie.immersiveenchanting.networking.packets.UnlockedEnchantmentsPacket;
import me.alfie.immersiveenchanting.networking.packets.UpdateToolSlotPacket;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerPayloadHandler {

    /**
     * Apply an enchantment to an item.
     * @param packet
     * @param context
     */
    public static void onEnchantItem(final EnchantItemPacket packet, final IPayloadContext context) {
        Player player = context.player();
        if (player == null) return;
        Level level = player.level();

        AbstractContainerMenu enchantingTableMenu = player.containerMenu;
        ItemStack itemToEnchant = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.TOOL.ordinal()).getItem();

        RegistryAccess registryAccess = player.registryAccess();
        Optional<Holder.Reference<Enchantment>> enchantmentHolder = ImmersiveEnchanting.getEnchantmentHolder(
                registryAccess,
                packet.enchantment()
        );

        Holder<Enchantment> enchantment = enchantmentHolder.orElseThrow(() ->
                new IllegalStateException("Enchantment not found: " + packet.enchantment())
        );

        // Check if any mod limits the number of enchantments / prevents enchantment
        if (!Compat.canEnchant(itemToEnchant, enchantment)) {
            level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1, 1);
            return;
        }

        //Check enchantment cost
        ItemStack costSlotItemStack = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.COST.ordinal()).getItem();
        ItemStack requiredItemCostStack = EnchantmentCostRegistry.getServerRegistry().getEnchantmentCost(packet.enchantment()).getLevel(packet.enchantmentLevel()).asItemStack();

        ItemStack requiredLapisCost = EnchantmentCostRegistry.getServerRegistry().getLapisCost();
        ItemStack lapisSlotStack = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.LAPIS.ordinal()).getItem();

        boolean hasEnoughCost = player.hasInfiniteMaterials() ||
                (requiredLapisCost.isEmpty() ||
                (lapisSlotStack.is(requiredLapisCost.getItem()) && lapisSlotStack.getCount() >= requiredLapisCost.getCount()))
                && (requiredItemCostStack.isEmpty() ||
                (costSlotItemStack.is(requiredItemCostStack.getItem()) &&
                        costSlotItemStack.getCount() >= requiredItemCostStack.getCount()));


        if (hasEnoughCost) {
            if (!player.hasInfiniteMaterials()) {
                enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.LAPIS.ordinal()).getItem()
                        .shrink(requiredLapisCost.getCount()); //Use enchantmnet cost registry
                if (!requiredItemCostStack.isEmpty()) {
                    costSlotItemStack.shrink(requiredItemCostStack.getCount()); //Use enchantment cost if not air
                }
            }

            //Enchant item server side
            itemToEnchant.enchant(
                    enchantment,
                    packet.enchantmentLevel()
            );

            player.awardStat(Stats.ENCHANT_ITEM);

            if (player instanceof ServerPlayer serverPlayer) {
                // Number is levels spent - using 1 to as a compatible default value
                // (Adjust if optional enchantment cost extensions in the future may include xp cost)
                CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, itemToEnchant, 1);
            }

            if (packet.enchantmentLevel() == EnchantmentCostRegistry.getServerRegistry().getEnchantmentCost(packet.enchantment()).getHighestLevel()) {
                //Sound FX for highest tier.
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                //Sound FX for normal tier.
                level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        } else {
            //If unable to enchant
            level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public static void onGetBookshelfContentsPacket(final GetBookshelfContentsPacket packet, final IPayloadContext context) {
        BlockPos tablePos = new BlockPos(packet.blockPosX(), packet.blockPosY(), packet.blockPosZ());
        checkBookshelvesAndUpdateClient(tablePos, context.player().level(), (ServerPlayer) context.player());
    }

    /**
     * Get the enchantment resource id for an ancient book.
     * @param book
     * @return
     */
    @Nullable
    public static ResourceKey<Enchantment> getAncientBookResourceKey(ItemStack book) {
        if (book.has(ModDataComponents.ENCHANTMENT)) {
            String resource = EnchantmentDataComponent.getEnchantmentData(book);

            if (resource != null) {
                return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(resource));
            }
        }

        return null;
    }

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
                    ResourceKey<Enchantment> key = getAncientBookResourceKey(book);

                    if (key != null) {
                        unlockedEnchantments.add(key);
                    }
                }
            }
        }

        //Unlock all enchantments if creative bookshelf is near
        if(isCreativeBookshelfNearby(tablePos, level)) {
            //Clear unlockedEnchantments from the bookshelf search
            unlockedEnchantments.clear();

            //Add all enchantments that exist
            RegistryAccess registryAccess = level.registryAccess();
            Registry<Enchantment> enchantmentRegistry = ImmersiveEnchanting.getEnchantmentRegistry(registryAccess);
            List<Holder.Reference<Enchantment>> allEnchantments = enchantmentRegistry.asLookup().listElements().toList();

            unlockedEnchantments = allEnchantments.stream()
                    .map(Holder.Reference::key)
                    .toList();
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

        for (int dy = 0; dy <= 1; dy++) { // check table level and level above
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    // Skip inner 3x3 square; only outer ring
                    if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;

                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    BlockEntity blockEntity = level.getBlockEntity(checkPos);

                    if (blockEntity instanceof ChiseledBookShelfBlockEntity) {
                        blockEntities.add(blockEntity);
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

    public static void onUpdateSlotPacket(UpdateToolSlotPacket updateToolSlotPacket, IPayloadContext context) {
        int mode = updateToolSlotPacket.mode();

        Slot toolSlot = context.player().containerMenu.getSlot(EnchantingTableMenu.SLOTS.TOOL.ordinal());
        if(mode == UpdateToolSlotPacket.MODE.TAKE.ordinal()) {
            ItemStack itemStack = toolSlot.getItem();
            context.player().containerMenu.setCarried(itemStack.copyAndClear());
            toolSlot.setChanged();
        } else if (mode == UpdateToolSlotPacket.MODE.PLACE.ordinal()) {
            ItemStack carriedStack = context.player().containerMenu.getCarried();
            toolSlot.safeInsert(carriedStack);
            toolSlot.setChanged();
        }
    }
}

