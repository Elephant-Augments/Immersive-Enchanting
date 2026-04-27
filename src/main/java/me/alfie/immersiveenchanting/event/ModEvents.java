package me.alfie.immersiveenchanting.event;

import me.alfie.immersiveenchanting.ImmersiveEnchanting;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.command.ModCommands;
import me.alfie.immersiveenchanting.creativetab.ModCreativeTab;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostDatapack;
import me.alfie.immersiveenchanting.datapack.manager.ClientDatapackManager;
import me.alfie.immersiveenchanting.datapack.manager.ServerDatapackManager;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.gui.EnchantingTableMenu;
import me.alfie.immersiveenchanting.gui.ModMenus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.addListener(CostDatapack::registerServerDatapack);

        MinecraftForge.EVENT_BUS.addListener(ServerDatapackManager::onServerStart);
        MinecraftForge.EVENT_BUS.addListener(ServerDatapackManager::onServerFinished);
        MinecraftForge.EVENT_BUS.addListener(ServerDatapackManager::onServerReload);
        MinecraftForge.EVENT_BUS.addListener(ServerDatapackManager::onServerStop);

        MinecraftForge.EVENT_BUS.addListener(ServerDatapackManager::resolveEnchantmentHolders);
        MinecraftForge.EVENT_BUS.addListener(ClientDatapackManager::resolveEnchantmentHolders);

        MinecraftForge.EVENT_BUS.addListener(NodeSoundsDatapack::registerServerDatapack);

        MinecraftForge.EVENT_BUS.addListener(ModCommands::registerCommands);

        MinecraftForge.EVENT_BUS.addListener(EnchantingTableMenu::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(ImmersiveEnchanting::disableEnchantedBookVillagerTrades);
        modEventBus.addListener(ModMenus::registerScreens);

        modEventBus.addListener(TooltipDescriptionExtensions::registerInternalTooltipDescriptions);

        modEventBus.addListener(ModCreativeTab::buildCreativeTab);
    }

}
