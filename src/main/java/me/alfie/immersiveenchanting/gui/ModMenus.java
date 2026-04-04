package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, ImmersiveEnchanting.MODID);
    public static final Supplier<MenuType<EnchantingTableMenu>> ENCHANTING_TABLE_MENU = REGISTER.register(
            "enchanting_table_menu", () -> new MenuType<>(
                    (containerId, playerInventory) ->
                            new EnchantingTableMenu(containerId, playerInventory, null, null),
                    FeatureFlags.DEFAULT_FLAGS
            )
    );

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ENCHANTING_TABLE_MENU.get(), EnchantingTableScreen::new);
    }
}
