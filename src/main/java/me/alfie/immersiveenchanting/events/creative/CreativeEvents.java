package me.alfie.immersiveenchanting.events.creative;

import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.item.AncientBook;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.util.HashSet;
import java.util.Set;

public class CreativeEvents {

    /**
     * Put items in the mod's Enchanting creative tab.
     * @param event
     */
    public static void onBuildModCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeTab.ENCHANTING_TAB.get()) {
            event.accept(new ItemStack(Items.ENCHANTING_TABLE));
            event.accept(new ItemStack(Items.CHISELED_BOOKSHELF));
            event.accept(new ItemStack(ModItems.CREATIVE_BOOKSHELF));
            event.accept(new ItemStack(Items.LAPIS_LAZULI));
            event.accept(new ItemStack(ModItems.BIBLIOCLASM_MUSIC_DISC.get()));
            event.accept(new ItemStack(ModItems.ARCANE_MEMORIES_MUSIC_DISC.get()));

            //Add all ancient books
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = event.getParameters().holders().lookupOrThrow(Registries.ENCHANTMENT);
            enchantmentRegistryLookup.listElements().forEach(holder -> {
                ResourceLocation id = holder.key().location();
                ItemStack ancientBookStack = new ItemStack(ModItems.ANCIENT_BOOK.get());
                AncientBook.setStoredEnchantment(ancientBookStack, holder);
                event.accept(ancientBookStack);
            });
        }

    }

}
