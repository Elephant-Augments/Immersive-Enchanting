package me.alfie.immersiveenchanting.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FillChiseledBookshelfProcessor extends StructureProcessor {

    private static final MapCodec<FillChiseledBookshelfProcessor> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SimpleLootEntry.CODEC.listOf().fieldOf("items").forGetter(e -> e.simpleLootTable)
            ).apply(instance, FillChiseledBookshelfProcessor::new));

    private final List<SimpleLootEntry> simpleLootTable;

    public FillChiseledBookshelfProcessor(List<SimpleLootEntry> simpleLootTable) {
        this.simpleLootTable = simpleLootTable;
    }



    protected static MapCodec<FillChiseledBookshelfProcessor> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return ModStructureProcessors.FILL_CHISELED_BOOKSHELF.get();
    }
}
