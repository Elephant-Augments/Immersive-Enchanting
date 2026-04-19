package me.alfie.immersiveenchanting.block;

import com.mojang.serialization.MapCodec;
import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

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
