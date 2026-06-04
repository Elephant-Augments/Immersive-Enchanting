package me.alfie.immersiveenchanting.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure processor that fills chiseled bookshelves with loot-defined items.
 *
 * <p>Each bookshelf slot is rolled independently using a weighted loot table.
 * Supports enchanted ancient book generation.
 */
public class FillChiseledBookshelfProcessor extends StructureProcessor {

    public static final Codec<FillChiseledBookshelfProcessor> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ChiseledBookshelfLootEntry.CODEC.listOf().fieldOf("items").forGetter(e -> e.simpleLootTable)
            ).apply(instance, FillChiseledBookshelfProcessor::new));


    private final List<ChiseledBookshelfLootEntry> simpleLootTable;

    public FillChiseledBookshelfProcessor(List<ChiseledBookshelfLootEntry> simpleLootTable) {
        this.simpleLootTable = simpleLootTable;
    }

    protected static Codec<FillChiseledBookshelfProcessor> codec() {
        return CODEC;
    }
    @Override protected @NotNull StructureProcessorType<?> getType() {return ModStructureProcessors.FILL_CHISELED_BOOKSHELF.get();}

    /**
     * Post-processes structure generation by filling chiseled bookshelves with loot.
     *
     * <p>Each bookshelf block has its 6 slots independently rolled using the loot table.
     * Existing structure data is preserved and extended with generated items.</p>
     *
     * @param level world access during structure generation
     * @param position origin position of the structure
     * @param referencePos reference offset position
     * @param originalBlockInfoList original unmodified structure blocks
     * @param processedBlockInfoList blocks after initial structure processing
     * @param settings structure placement settings
     * @return modified block list with filled bookshelves
     */
    @Override
    public @NotNull List<StructureTemplate.StructureBlockInfo> finalizeProcessing(@NotNull ServerLevelAccessor level,
                                                                                  @NotNull BlockPos position,
                                                                                  @NotNull BlockPos referencePos,
                                                                                  @NotNull List<StructureTemplate.StructureBlockInfo> originalBlockInfoList,
                                                                                  @NotNull List<StructureTemplate.StructureBlockInfo> processedBlockInfoList,
                                                                                  @NotNull StructurePlaceSettings settings) {
        List<StructureTemplate.StructureBlockInfo> processedBlocks = new ArrayList<>();
        for(StructureTemplate.StructureBlockInfo block : processedBlockInfoList) {

            if(block.state().is(Blocks.CHISELED_BOOKSHELF)) {
                final int slots = 6;
                for (int slot = 0; slot < slots; slot++) {
                    ItemStack stack = rollLootForSlot(level.getRandom());
                    block = setBookshelfItem(slot, stack, block, level.registryAccess());
                }
            }

            processedBlocks.add(block);
        }

        return super.finalizeProcessing(level, position, referencePos, originalBlockInfoList, processedBlocks, settings);
    }

    /**
     * Inserts an item into a specific slot of a chiseled bookshelf structure block.
     *
     * <p>Updates both the block entity NBT data and the visual slot occupancy state.</p>
     *
     * @param slot bookshelf slot index (0–5)
     * @param stack item to insert (empty = clears slot)
     * @param block structure block being modified
     * @param registryAccess registry access for encoding item data
     * @return updated structure block info
     */
    private StructureTemplate.StructureBlockInfo setBookshelfItem(int slot, ItemStack stack,
                                  StructureTemplate.StructureBlockInfo block, RegistryAccess registryAccess) {
        CompoundTag blockTag = block.nbt() != null ? block.nbt().copy() : new CompoundTag();

        final String itemTag = "Items";
        final String slotTag = "Slot";

        ListTag blockItemsTag = blockTag.contains(itemTag, ListTag.TAG_LIST) ?
                blockTag.getList(itemTag, ListTag.TAG_COMPOUND) : new ListTag();

        if(!stack.isEmpty()) {
            CompoundTag stackTag = stackToTag(stack);
            stackTag.putByte(slotTag, (byte) slot);

            blockItemsTag.add(stackTag);
            blockTag.put(itemTag, blockItemsTag);
        }

        BlockState state = block.state();
        state = state.setValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot), !stack.isEmpty());


        return new StructureTemplate.StructureBlockInfo(block.pos(), state, blockTag);
    }

    /**
     * Serializes an ItemStack into a raw NBT tag using registry-aware encoding.
     *
     * @param stack item stack to serialize
     * @return NBT compound representing the item stack
     */
    private CompoundTag stackToTag(ItemStack stack) {
        return stack.save(new CompoundTag());
    }

    /**
     * Rolls a random item for a bookshelf slot based on the loot table.
     *
     * <p>Each entry is selected randomly, then tested against its per-slot chance.
     * Ancient books may receive a randomly assigned enchantment.</p>
     *
     * @param random random source for loot generation
     * @return generated item stack, or empty if roll fails
     */
    private ItemStack rollLootForSlot(RandomSource random) {
        ChiseledBookshelfLootEntry lootEntry = simpleLootTable.get(random.nextInt(simpleLootTable.size()));

        float chance = lootEntry.chancePerSlot();
        float roll = random.nextFloat();

        if(roll < chance) {
            ItemStack stack = new ItemStack(lootEntry.item(), 1);
            if(stack.is(ModItems.ANCIENT_BOOK.get())) EnchantmentUtil.setStoredEnchantment(stack,
                    CostRegistry.server().getRandomEnchantment(random));

            return stack;
        }
        return ItemStack.EMPTY;
    }
}
