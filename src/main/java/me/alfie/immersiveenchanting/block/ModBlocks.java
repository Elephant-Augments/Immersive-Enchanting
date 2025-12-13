package me.alfie.immersiveenchanting.block;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ImmersiveEnchanting.MODID);

    public static final DeferredBlock<Block> CREATIVE_BOOKSHELF = BLOCKS.register(
            "creative_bookshelf",
            () -> new CreativeBookshelf(BlockBehaviour.Properties.of()
                    .destroyTime(2.0f)
                    .explosionResistance(10.0f)
                    .sound(SoundType.STONE)
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }


}
