package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, ImmersiveEnchanting.MODID);

    public static final RegistryObject<MenuType<EnchantingTableMenu>> ENCHANTING_TABLE_MENU =
            REGISTER.register("enchanting_table_menu",
                    () -> IForgeMenuType.create(EnchantingTableMenu::new));

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }

    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> MenuScreens.register(ModMenus.ENCHANTING_TABLE_MENU.get(), EnchantingTableScreen::new)
        );
    }
}
