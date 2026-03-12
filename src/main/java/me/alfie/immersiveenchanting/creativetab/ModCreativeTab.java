package me.alfie.immersiveenchanting.creativetab;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTab {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB_DEFERRED_REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImmersiveEnchanting.MODID);

    public static final Supplier<CreativeModeTab> ENCHANTING_TAB = CREATIVE_MODE_TAB_DEFERRED_REGISTER.register("enchanting", () -> CreativeModeTab.builder()
            //Set the title of the tab. Don't forget to add a translation!
            .title(Component.translatable("itemGroup.immersiveenchanting.enchanting"))
            //Set the icon of the tab.
            .icon(() -> new ItemStack(Items.ENCHANTING_TABLE))
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB_DEFERRED_REGISTER.register(eventBus);
    }
}
