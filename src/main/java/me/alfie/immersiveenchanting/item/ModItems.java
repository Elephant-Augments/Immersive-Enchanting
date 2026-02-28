package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveEnchanting.MODID);
    public static final Supplier<Item> ANCIENT_BOOK = ITEMS.registerItem(
            "ancient_book",
            AncientBook::new,
            new Item.Properties()
    );

    public static final Supplier<Item> BIBLIOCLASM_MUSIC_DISC = ITEMS.registerItem(
            "music_disc_biblioclasm",
            BiblioclasmDisc::new,
            new Item.Properties()
    );

    public static final Supplier<Item> ARCANE_MEMORIES_MUSIC_DISC = ITEMS.registerItem(
            "music_disc_arcane_memories",
            ArcaneMemoriesDisc::new,
            new Item.Properties()
    );

    public static final DeferredHolder<Item, BlockItem> CREATIVE_BOOKSHELF =
            ITEMS.register("creative_bookshelf",
                    () -> new BlockItem(ModBlocks.CREATIVE_BOOKSHELF.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
