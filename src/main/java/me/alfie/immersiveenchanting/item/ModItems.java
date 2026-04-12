package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ImmersiveEnchanting.MODID);
    public static final Supplier<Item> ANCIENT_BOOK = ITEMS.registerItem(
            "ancient_book",
            AncientBook::new,
            properties -> properties
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
