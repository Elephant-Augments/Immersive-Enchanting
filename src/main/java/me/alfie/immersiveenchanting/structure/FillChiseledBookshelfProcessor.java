package me.alfie.immersiveenchanting.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
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

public class FillChiseledBookshelfProcessor extends StructureProcessor {

    public static final Codec<FillChiseledBookshelfProcessor> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    SimpleLootEntry.CODEC.listOf().fieldOf("items").forGetter(e -> e.simpleLootTable)
            ).apply(instance, FillChiseledBookshelfProcessor::new));

    private final List<SimpleLootEntry> simpleLootTable;

    public FillChiseledBookshelfProcessor(List<SimpleLootEntry> simpleLootTable) {
        this.simpleLootTable = simpleLootTable;
    }

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

    private CompoundTag stackToTag(ItemStack stack) {
        return stack.save(new CompoundTag());
    }

    private ItemStack rollLootForSlot(RandomSource random) {
        SimpleLootEntry lootEntry = simpleLootTable.get(random.nextInt(simpleLootTable.size()));

        float chance = lootEntry.chancePerSlot();
        float roll = random.nextFloat();

        if(roll < chance) {
            List<ItemStack> itemStacks = lootEntry.itemOrTag().getItemStacks(1);
            int randomIndex = random.nextInt(itemStacks.size());

            ItemStack randomStack = itemStacks.get(randomIndex);
            if(randomStack.is(ModItems.ANCIENT_BOOK.get())) EnchantmentUtil.setStoredEnchantment(randomStack,
                    CostRegistry.server().getRandomEnchantment(random));

            return randomStack;
        }
        return ItemStack.EMPTY;
    }

    protected static Codec<FillChiseledBookshelfProcessor> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return ModStructureProcessors.FILL_CHISELED_BOOKSHELF.get();
    }
}
