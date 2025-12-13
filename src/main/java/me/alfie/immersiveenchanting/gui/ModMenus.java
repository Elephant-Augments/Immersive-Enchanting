package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(Registries.MENU, ImmersiveEnchanting.MODID);
    public static final Supplier<MenuType<EnchantingTableMenu>> ENCHANTING_TABLE_MENU = MENU_REGISTER.register(
            "enchanting_table_menu",
            () -> new MenuType<>(
                    (containerId, playerInventory) ->
                            // At registration time, you don't have a block entity/position, so pass null
                            new EnchantingTableMenu(containerId, playerInventory, (Level) null, (BlockPos) null),
                    FeatureFlags.DEFAULT_FLAGS
            )
    );

    public static void register(IEventBus eventBus) {
        MENU_REGISTER.register(eventBus);
    }

    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ENCHANTING_TABLE_MENU.get(), EnchantingTableScreen::new);
    }
}
