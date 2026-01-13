package me.alfie.immersiveenchanting.structure;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.block.ModBlocks;
import me.alfie.immersiveenchanting.item.AncientBook;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModStructureProcessors {

    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPE_DEFERRED_REGISTER =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_PROCESSOR, ImmersiveEnchanting.MODID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<FillChiseledBookshelfProcessor>>
    FILL_CHISELED_BOOKSHELF = STRUCTURE_PROCESSOR_TYPE_DEFERRED_REGISTER.register("fill_chiseled_bookshelf",
            () -> FillChiseledBookshelfProcessor::codec);

    public static void register(IEventBus eventBus) {
        STRUCTURE_PROCESSOR_TYPE_DEFERRED_REGISTER.register(eventBus);
    }
}
