package me.alfie.immersiveenchanting.creativetab;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

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
            event.accept(new ItemStack(ModItems.CREATIVE_BOOKSHELF.get()));
            event.accept(new ItemStack(Items.LAPIS_LAZULI));

            for(Holder<Enchantment> enchantmentHolder : EnchantmentUtil.getAllRegisteredEnchantments(event.getParameters().holders())) {
                ItemStack stack = new ItemStack(ModItems.ANCIENT_BOOK.get());
                EnchantmentUtil.setStoredEnchantment(stack, enchantmentHolder);
                event.accept(stack);
            }

            event.accept(new ItemStack(ModItems.ARCANE_MEMORIES_MUSIC_DISC.get()));
            event.accept(new ItemStack(ModItems.BIBLIOCLASM_MUSIC_DISC.get()));
        }
    }
}
