package me.alfie.immersiveenchanting.event;

import me.alfie.immersiveenchanting.api.ApiPostEvents;
import me.alfie.immersiveenchanting.api.enchanting_tab.NodeClickHandlerRegistry;
import me.alfie.immersiveenchanting.command.ModCommands;
import me.alfie.immersiveenchanting.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.datapack.manager.ServerDatapackManager;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.networking.ModPackets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(CostDatapack::register);
        NeoForge.EVENT_BUS.addListener(NodeSoundsDatapack::register);


        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerStart);
        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerFinished);
        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerReload);
        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::onServerStop);

        NeoForge.EVENT_BUS.addListener(ServerDatapackManager::resolveEnchantmentHolders);
        NeoForge.EVENT_BUS.addListener(ClientDatapackManager::resolveEnchantmentHolders);


        NeoForge.EVENT_BUS.addListener(ModCommands::registerCommands);
        modEventBus.addListener(ModMenus::registerScreens);

        modEventBus.addListener(ModPackets::registerClient);
        modEventBus.addListener(ModPackets::registerServer);

        modEventBus.addListener(ModCreativeTab::buildCreativeTab);

        registerPostEvents(modEventBus);
        registerInternalApiEvents();
    }

    private static void registerPostEvents(IEventBus modEventBus) {
        modEventBus.addListener(ApiPostEvents::postRegisterTooltipDescriptionsEvent);
        modEventBus.addListener(ApiPostEvents::postRegisterNodeClickHandlersEvent);
    }

    private static void registerInternalApiEvents() {
        NeoForge.EVENT_BUS.addListener(TooltipDescriptionExtensions::registerInternalTooltipDescriptions);
        NeoForge.EVENT_BUS.addListener(NodeClickHandlerRegistry::registerInternalNodeInteractions);
    }

}
