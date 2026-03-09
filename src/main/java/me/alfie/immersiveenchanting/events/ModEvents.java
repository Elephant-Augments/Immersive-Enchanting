package me.alfie.immersiveenchanting.events;

import me.alfie.immersiveenchanting.events.client.ClientEvents;
import me.alfie.immersiveenchanting.events.command.CommandEvents;
import me.alfie.immersiveenchanting.events.creative.CreativeEvents;
import me.alfie.immersiveenchanting.events.datapack.DatapackEvents;
import me.alfie.immersiveenchanting.events.enchanting.EnchantingTableEvents;
import me.alfie.immersiveenchanting.events.gameplay.GameplayEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(ClientEvents.class);
        NeoForge.EVENT_BUS.register(CommandEvents.class);
        NeoForge.EVENT_BUS.register(DatapackEvents.class);
        NeoForge.EVENT_BUS.register(EnchantingTableEvents.class);
        NeoForge.EVENT_BUS.register(GameplayEvents.class);

        modEventBus.addListener(ClientEvents::loadClientResources);
        modEventBus.addListener(ClientEvents::registerInternalEnchantingTooltips);
        modEventBus.addListener(CreativeEvents::onBuildModCreativeTab);
        modEventBus.addListener(DatapackEvents::setDatapackClient);
    }
}
