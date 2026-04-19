package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveEnchanting.MODID);
    public static final Supplier<Item> ANCIENT_BOOK = ITEMS.registerItem(
            "ancient_book",
            AncientBook::new,
            properties -> properties
    );

    public static final DeferredItem<@NotNull BlockItem> CREATIVE_BOOKSHELF_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.CREATIVE_BOOKSHELF_BLOCK,
            properties -> properties.rarity(Rarity.EPIC)
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
