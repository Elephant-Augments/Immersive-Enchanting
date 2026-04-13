package me.alfie.immersiveenchanting.item;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentUtil;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.manager.ClientCostManager;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModCreativeTab {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ImmersiveEnchanting.MODID);

    public static final Supplier<CreativeModeTab> ENCHANTING_TAB = CREATIVE_REGISTER.register("enchanting", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.immersiveenchanting.enchanting"))
            .icon(() -> new ItemStack(Items.ENCHANTING_TABLE))
            .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_REGISTER.register(eventBus);
    }

    public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if(event.getTab() == ModCreativeTab.ENCHANTING_TAB.get()) {
            event.accept(new ItemStack(Items.ENCHANTING_TABLE));
            event.accept(new ItemStack(Items.CHISELED_BOOKSHELF));
            event.accept(new ItemStack(Items.LAPIS_LAZULI));

            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = event.getParameters().holders().lookupOrThrow(Registries.ENCHANTMENT);
            enchantmentRegistryLookup.listElements().forEach(holder -> {
                        ItemStack stack = new ItemStack(ModItems.ANCIENT_BOOK.get());
                        EnchantmentUtil.setStoredEnchantment(stack, holder, null);
                        event.accept(stack);
                    });
        }
    }
}
