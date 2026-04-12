package me.alfie.immersiveenchanting.event;

import me.alfie.immersiveenchanting.item.ModCreativeTab;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.EnchantmentCostDatapack;
import me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack;
import me.alfie.immersiveenchanting.gui.ModMenus;
import me.alfie.immersiveenchanting.networking.ModPackets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(EnchantmentCostDatapack::registerServerDatapack);
        modEventBus.addListener(EnchantmentCostDatapack::registerClientDatapack);
        NeoForge.EVENT_BUS.addListener(EnchantmentCostDatapack::resolveEnchantmentHolders);

        NeoForge.EVENT_BUS.addListener(NodeSoundsDatapack::registerServerDatapack);
        modEventBus.addListener(NodeSoundsDatapack::registerClientDatapack);

        modEventBus.addListener(ModMenus::registerScreens);
        modEventBus.addListener(ModPackets::registerClient);
        modEventBus.addListener(ModPackets::registerServer);

        modEventBus.addListener(TooltipDescriptionExtensions::registerInternalTooltipDescriptions);

        modEventBus.addListener(ModCreativeTab::buildCreativeTab);
    }

}
