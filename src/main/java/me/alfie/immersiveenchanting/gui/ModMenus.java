package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENU_REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ImmersiveEnchanting.MODID);
    public static final RegistryObject<MenuType<EnchantingTableMenu>> ENCHANTING_TABLE_MENU =
            MENU_REGISTER.register("enchanting_table_menu",
                    () -> IForgeMenuType.create(EnchantingTableMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_REGISTER.register(eventBus);
    }
}
