package me.alfie.immersiveenchanting.block;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImmersiveEnchanting.MODID);

    public static final DeferredBlock<@NotNull Block> CREATIVE_BOOKSHELF_BLOCK = BLOCKS.registerBlock(
            "creative_bookshelf",
            CreativeBookshelfBlock::new,
            props -> props);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
