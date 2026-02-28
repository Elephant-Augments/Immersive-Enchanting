package me.alfie.immersiveenchanting.networking;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.block.CreativeBookshelf;
import me.alfie.immersiveenchanting.compat.ModCompat;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.datacomponent.ModDataComponents;
import me.alfie.immersiveenchanting.datacomponent.ReplicatedDataComponent;
import me.alfie.immersiveenchanting.datapack.cost.CostDefinition;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostDatapackHandler;
import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.lootmodifier.AncientBookLootModifier;
import me.alfie.immersiveenchanting.networking.packets.*;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.*;

public class ServerPayloadHandler {

    public static void onReplicateBookPacket(final ReplicateBookPacket packet, final IPayloadContext context) {
        Level level = context.player().level();
        Player player = context.player();

        if(context.player().containerMenu instanceof EnchantingTableMenu enchantingTableMenu) {
            if(player.experienceLevel >= 10 && enchantingTableMenu.getCostSlotItem().is(Items.WRITABLE_BOOK)
                    || player.isCreative()) {

                player.giveExperienceLevels(-10);
                enchantingTableMenu.getCostSlotItem().shrink(1);
                BlockPos tablePos = enchantingTableMenu.getBlockPos();

                ItemStack oldStack = enchantingTableMenu.getToolSlotItem().copyAndClear();
                ItemStack newStack = oldStack.copy();

                //Copied books cannot be transmuted
                newStack.set(ModDataComponents.REPLICATED, new ReplicatedDataComponent(true));

                //Create 2 entities
                for (int i = 0; i < 2; i++) {
                    ItemStack stackToSpawn = (i == 0) ? oldStack : newStack;
                    ItemEntity entity = new ItemEntity(
                            level,
                            tablePos.getX() + 0.5,
                            tablePos.getY() + 1,
                            tablePos.getZ() + 0.5,
                            stackToSpawn);
                    entity.setPickUpDelay(40);
                    entity.setDeltaMovement(Vec3.ZERO);
                    level.addFreshEntity(entity);
                }


                //Play effects
                level.playSound(null, tablePos, SoundEvents.ALLAY_ITEM_GIVEN,
                        SoundSource.BLOCKS, 0.7F, 1.2F);
                level.playSound(null, tablePos, SoundEvents.VILLAGER_WORK_CARTOGRAPHER,
                        SoundSource.BLOCKS, 0.8F, 1.5F);
                level.playSound(null, tablePos, SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.BLOCKS, 0.5F, 1.5F);
                level.playSound(null, tablePos, SoundEvents.ILLUSIONER_MIRROR_MOVE,
                        SoundSource.BLOCKS, 0.4F, 1.2F);


                int particleCount = 30;
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.END_ROD,
                        tablePos.getX() + 0.5,
                        tablePos.getY() + 1,
                        tablePos.getZ() + 0.5,
                        particleCount,
                        0.1, 0.1, 0.1,
                        0.1);

                player.closeContainer();
            } else {
                //If unable to enchant
                level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public static void onTransmuteBookPacket(final TransmuteBookPacket packet, final IPayloadContext context) {

        Player player = context.player();
        Level level = player.level();
        AbstractContainerMenu menu = player.containerMenu;

        if(menu instanceof EnchantingTableMenu enchantingTableMenu) {
            ItemStack ancientBookStack = enchantingTableMenu.getToolSlotItem();
            Set<Holder<Enchantment>> unlockedEnchantments = enchantingTableMenu.getUnlockedEnchantments();

            //Get all enchantments
            List<Holder.Reference<Enchantment>> enchantments = new ArrayList<>(
                    AncientBookLootModifier.getAllEnchantments(player.level()));

            //Remove all enchantments that are already in bookshelf
            enchantments.removeIf(unlockedEnchantments::contains);

            //If all enchantments are unlocked already, pick any random enchantment.
            if(enchantments.isEmpty()) {
                enchantments = new ArrayList<>(
                        AncientBookLootModifier.getAllEnchantments(player.level()));
            }

            Random random = new Random();
            int randomIndex = random.nextInt(enchantments.size());
            Holder<Enchantment> randomEnchantment = enchantments.get(randomIndex);

            //If has 10 levels
            boolean isBookReplicated = ReplicatedDataComponent.isReplicated(ancientBookStack);
            if(player.experienceLevel >= 10 || player.isCreative() && !isBookReplicated) {
                player.giveExperienceLevels(-10);

                //Save old enchantment for text
                Registry<Enchantment> enchantmentRegistry = ImmersiveEnchanting.getEnchantmentRegistry(level.registryAccess());
                Enchantment oldEnchantment = enchantmentRegistry.get(AncientBook.getStoredEnchantment(ancientBookStack, level));

                AncientBook.setStoredEnchantment(ancientBookStack, randomEnchantment);
                BlockPos tablePos = enchantingTableMenu.getBlockPos();

                level.playSound(null, tablePos, SoundEvents.ENDER_CHEST_OPEN,
                        SoundSource.BLOCKS, 0.4F, 1.0F);
                level.playSound(null, tablePos, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                        SoundSource.BLOCKS, 0.8F, 0.8F);
                level.playSound(null, tablePos, SoundEvents.EVOKER_CAST_SPELL,
                        SoundSource.BLOCKS, 0.8F, 1F);
                level.playSound(null, tablePos, SoundEvents.EVOKER_PREPARE_SUMMON,
                        SoundSource.BLOCKS, 0.1F, 1.2F);

                ItemStack stack = enchantingTableMenu.getToolSlotItem().copyAndClear();

                ItemEntity entity = new ItemEntity(
                        level,
                        tablePos.getX() + 0.5,
                        tablePos.getY() + 1,
                        tablePos.getZ() + 0.5,
                        stack);
                entity.setPickUpDelay(40);
                entity.setDeltaMovement(Vec3.ZERO);

                level.addFreshEntity(entity);

                int particleCount = 70;
                ((ServerLevel) level).sendParticles(
                        ParticleTypes.ENCHANT,
                        tablePos.getX() + 0.5,
                        tablePos.getY() + 1,
                        tablePos.getZ() + 0.5,
                        particleCount,
                        0.2, 0.2, 0.2,
                        0.1);

                ((ServerLevel) level).sendParticles(
                        ParticleTypes.GLOW,
                        tablePos.getX() + 0.5,
                        tablePos.getY() + 1,
                        tablePos.getZ() + 0.5,
                        particleCount,
                        0.2, 0.2, 0.2,
                        0.1);


                //Get the enchantment from the registry.
                Enchantment newEnchantment = enchantmentRegistry.get(randomEnchantment.getKey());

                Component enchantmentName = newEnchantment.description().copy().withStyle(ChatFormatting.GOLD);

                Component text = Component.translatable(
                        "gui.immersiveenchanting.transmuted_to",
                                enchantmentName)
                        .withStyle(ChatFormatting.GRAY);


                player.displayClientMessage(
                        text,
                        true
                );

                player.closeContainer();
            } else {
                //If unable to enchant
                level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            }


        }
    }

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

        // Currently only for Enchant Limiter.
        // Currently, this check only exists on NeoForge 1.21.1 as Enchant Limiter is not available on Forge 1.20.1.
        if (!ModCompat.canEnchant(itemToEnchant, enchantment)) {
            level.playSound(null, player.blockPosition(), SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1, 1);
            return;
        }

        //Check enchantment cost
        ItemStack costSlotItemStack = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.COST.ordinal()).getItem();
        List<ItemStack> insertedItems = new ArrayList<>();
        insertedItems.add(costSlotItemStack);

        int playerXp = player.experienceLevel;

        //TODO Use .isCostValid?
        CostDefinition costNode = EnchantmentCostRegistry.getServerRegistry().getEnchantmentCost(packet.enchantment()).getCostNodeForLevel(packet.enchantmentLevel());
        boolean costSlotIsValid = EnchantmentCostRegistry.isCostValid(costNode, insertedItems, playerXp);

        List<Item> validEnchantingFuels = EnchantmentCostDatapackHandler.getValidEnchantingFuels();
        ItemStack enchantingFuel = enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.ENCHANTING_FUEL.ordinal()).getItem();
        boolean enchantingFuelIsValid = validEnchantingFuels.contains(enchantingFuel.getItem());

        boolean hasEnoughCost = player.hasInfiniteMaterials() ||
                ( (validEnchantingFuels.isEmpty() || enchantingFuelIsValid)
                        && costSlotIsValid);

        if (hasEnoughCost) {
            if (!player.hasInfiniteMaterials()) {
                enchantingTableMenu.getSlot(EnchantingTableMenu.SLOTS.ENCHANTING_FUEL.ordinal()).getItem()
                        .shrink(1); //Todo use config

                //TODO shrink by correct amount from cost
                //costSlotItemStack.shrink(requiredItemCostStack.getCount()); //Use enchantment cost if not air
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
    @Deprecated(forRemoval = true)
    public static ResourceKey<Enchantment> getAncientBookResourceKey(ItemStack book) {
        if (book.has(DataComponents.STORED_ENCHANTMENTS)) {
            ItemEnchantments itemEnchantments = book.get(DataComponents.STORED_ENCHANTMENTS);
            List<Holder<Enchantment>> enchantments = itemEnchantments.keySet().stream().toList();
            Holder<Enchantment> enchantmentHolder = enchantments.getFirst();

            return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(enchantmentHolder.getRegisteredName()));
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
            RegistryAccess registryAccess = level.registryAccess();
            Registry<Enchantment> enchantmentRegistry = ImmersiveEnchanting.getEnchantmentRegistry(registryAccess);
            List<Holder.Reference<Enchantment>> allEnchantments = enchantmentRegistry.asLookup().listElements().toList();

            unlockedEnchantments = allEnchantments.stream()
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

        for (int dy = 0; dy <= ServerConfig.getBookshelfSearchHeight()-1; dy++) { // check table level and level above
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

