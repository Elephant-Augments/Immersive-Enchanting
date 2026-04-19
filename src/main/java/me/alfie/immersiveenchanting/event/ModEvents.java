package me.alfie.immersiveenchanting.event;

import me.alfie.immersiveenchanting.command.ModCommands;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.manager.ClientCostManager;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.manager.ServerCostManager;
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
        NeoForge.EVENT_BUS.addListener(CostDatapack::registerServerDatapack);

        NeoForge.EVENT_BUS.addListener(ServerCostManager::onServerStart);
        NeoForge.EVENT_BUS.addListener(ServerCostManager::onServerFinished);
        NeoForge.EVENT_BUS.addListener(ServerCostManager::onServerReload);
        NeoForge.EVENT_BUS.addListener(ServerCostManager::onServerStop);

        NeoForge.EVENT_BUS.addListener(ServerCostManager::resolveEnchantmentHolders);
        NeoForge.EVENT_BUS.addListener(ClientCostManager::resolveEnchantmentHolders);

        NeoForge.EVENT_BUS.addListener(NodeSoundsDatapack::registerServerDatapack);
        modEventBus.addListener(NodeSoundsDatapack::registerClientDatapack);

        NeoForge.EVENT_BUS.addListener(ModCommands::registerCommands);
        modEventBus.addListener(ModMenus::registerScreens);

        modEventBus.addListener(ModPackets::registerClient);
        modEventBus.addListener(ModPackets::registerServer);

        modEventBus.addListener(TooltipDescriptionExtensions::registerInternalTooltipDescriptions);

        modEventBus.addListener(ModCreativeTab::buildCreativeTab);
    }

}
