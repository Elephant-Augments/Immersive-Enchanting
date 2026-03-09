package me.alfie.immersiveenchanting.events.client;

import me.alfie.immersiveenchanting.api.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.datapack.EnchantmentMetadataRegistry;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ClientEvents {

    public static void loadClientResources(FMLClientSetupEvent event) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        EnchantmentMetadataRegistry.loadIcons(resourceManager);
    }

    public static void registerInternalEnchantingTooltips(FMLLoadCompleteEvent event) {
        TooltipDescriptionExtensions.registerInternalTooltipDescriptions();
    }

    /**
     * Hide the default enchantment tooltip for ancient books. Custom tooltip is rendered in AncientBook class.
     * @param event
     */
    @SubscribeEvent
    public static void hideAncientBookEnchantmentTooltip(ItemTooltipEvent event) {
        if(event.getItemStack().is(ModItems.ANCIENT_BOOK.get())) {
            event.getToolTip().removeIf(component -> component.getContents() instanceof TranslatableContents contents
                    && contents.getKey().startsWith("enchantment."));
        }

    }




}
