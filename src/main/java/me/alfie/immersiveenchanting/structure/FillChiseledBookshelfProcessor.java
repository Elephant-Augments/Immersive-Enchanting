package me.alfie.immersiveenchanting.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;

public class FillChiseledBookshelfProcessor extends StructureProcessor {

    //85 bookshelves per structure
    private final float fillChance;
    private final float musicDiscChance = 0.002f;

    public static final Codec<FillChiseledBookshelfProcessor> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("fill_chance")
                            .forGetter(p -> p.fillChance)
            ).apply(instance, FillChiseledBookshelfProcessor::new));

    protected static Codec<FillChiseledBookshelfProcessor> codec() {
        return CODEC;
    }

    public FillChiseledBookshelfProcessor(float fillChance) {
        this.fillChance = fillChance;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModStructureProcessors.FILL_CHISELED_BOOKSHELF.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor serverLevel, BlockPos offset, BlockPos pos, List<StructureTemplate.StructureBlockInfo> originalBlockInfos, List<StructureTemplate.StructureBlockInfo> processedBlockInfos, StructurePlaceSettings settings) {
        List<StructureTemplate.StructureBlockInfo> newProcessed = new ArrayList<>();
        for (StructureTemplate.StructureBlockInfo block : processedBlockInfos) {
            if (block.state().is(Blocks.CHISELED_BOOKSHELF)) {
                BlockPos blockPos = block.pos();

                //Overwrite block -> Add loot to bookshelves
                block = setChiseledBookshelfLoot(block,
                        settings.getRandom(blockPos),
                        serverLevel.getLevel());

            }
            newProcessed.add(block);
        }
        return super.finalizeProcessing(serverLevel, offset, pos, originalBlockInfos, newProcessed, settings);
    }

    /**
     * Uses the AncientBookLootModifier to randomly pick an enchantment. Adds the book to the bookshelf.
     * @param block
     * @param randomSource
     * @param serverLevel
     * @return Return the block to add to the processedBlockInfos list
     */
    private StructureTemplate.StructureBlockInfo setChiseledBookshelfLoot(StructureTemplate.StructureBlockInfo block,
                                         RandomSource randomSource,
                                         ServerLevel serverLevel) {
        //Try each slot
        for (int i = 0; i < 6; i++) {
            float randomRoll = randomSource.nextFloat();
            if (randomRoll < fillChance) {
                Holder<Enchantment> randomEnchantment = EnchantmentUtil.getRandomEnchantment(serverLevel, randomSource);
                ItemStack ancientBook = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
                AncientBook.setStoredEnchantment(ancientBook, randomEnchantment);
                block = setBookshelfSlot(block, i, ancientBook, serverLevel);
            } else if (randomRoll < fillChance + 0.3) { //30% chance for normal book
                block = setBookshelfSlot(block, i, new ItemStack(Items.BOOK, 1), serverLevel);

            } else if (randomRoll < fillChance + 0.3 + musicDiscChance) {
                block = setBookshelfSlot(block, i, new ItemStack(ModItems.ARCANE_MEMORIES_MUSIC_DISC.get(), 1), serverLevel);
            }
        }
        return block;
    }

    /**
     * Trigger warning: this is gross <br>
     * Minecraft does not expose a way to set a ChiseledBookshelfEntity slot without calling updateState() - which requires the BlockEntity to have a Level. <br>
     * Level is null during finaliseProcessing, which causes a worldgen crash if you try to use BlockEntity.
     * Here we are manually setting the bookshelf slot using NBT data to make the game happy and then updating the blockstate manually.
     * @param block
     * @param slot
     * @param stack
     * @param level
     * @return
     */
    private StructureTemplate.StructureBlockInfo setBookshelfSlot(
            StructureTemplate.StructureBlockInfo block,
            int slot,
            ItemStack stack,
            Level level) {

        // Get existing NBT or create a new one
        CompoundTag blockTag = block.nbt() != null ? block.nbt().copy() : new CompoundTag();

        // Get or create the "Items" list
        ListTag itemsTag = blockTag.contains("Items", 9) ? blockTag.getList("Items", 10) : new ListTag();

        // Convert the stack to NBT and set the slot
        CompoundTag stackTag = new CompoundTag();
        stack.save(stackTag);
        stackTag.putByte("Slot", (byte) slot);

        itemsTag.add(stackTag);
        blockTag.put("Items", itemsTag);

        //Update visual blockstate here
        BlockState state = block.state();
        state = state.setValue(ChiseledBookShelfBlock.SLOT_OCCUPIED_PROPERTIES.get(slot), true);

        // Return a new StructureBlockInfo with the updated NBT
        return new StructureTemplate.StructureBlockInfo(block.pos(), state, blockTag);
    }


}
