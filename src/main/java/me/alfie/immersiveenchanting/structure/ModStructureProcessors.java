package me.alfie.immersiveenchanting.structure;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructureProcessors {

    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPE_DEFERRED_REGISTER =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, ImmersiveEnchanting.MODID);

    public static final RegistryObject<StructureProcessorType<FillChiseledBookshelfProcessor>>
            FILL_CHISELED_BOOKSHELF = STRUCTURE_PROCESSOR_TYPE_DEFERRED_REGISTER.register("fill_chiseled_bookshelf",
            () -> StructureProcessorType.register("fill_chiseled_bookshelf", FillChiseledBookshelfProcessor.CODEC));


    public static void register(IEventBus eventBus) {
        STRUCTURE_PROCESSOR_TYPE_DEFERRED_REGISTER.register(eventBus);
    }

}
