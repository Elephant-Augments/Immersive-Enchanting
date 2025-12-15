package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveEnchanting.MODID);
    public static final RegistryObject<Item> ANCIENT_BOOK = ITEMS.register(
            "ancient_book",
            () -> new AncientBook(new Item.Properties())
    );

    public static final RegistryObject<BlockItem> CREATIVE_BOOKSHELF =
            ITEMS.register("creative_bookshelf",
                    () -> new BlockItem(ModBlocks.CREATIVE_BOOKSHELF.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
