package me.alfie.immersiveenchanting;

import me.alfie.immersiveenchanting.client.EnchantingTableItemRenderer;
import me.alfie.immersiveenchanting.client.ModKeyMappings;
import me.alfie.immersiveenchanting.compat.stylisheffects.StylishEffectsCompat;
import me.alfie.immersiveenchanting.compat.tooltip.EnchantmentTooltipReorder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.EventPriority;

@Mod(value = ImmersiveEnchanting.MODID, dist = Dist.CLIENT)
public class ImmersiveEnchantingClient {

    public ImmersiveEnchantingClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(ModKeyMappings::register);
        NeoForge.EVENT_BUS.addListener(StylishEffectsCompat::onScreenOpening);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, EnchantmentTooltipReorder::onItemTooltip);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(StylishEffectsCompat::register);
    }

    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.ENCHANTING_TABLE, EnchantingTableItemRenderer::new);
    }
}