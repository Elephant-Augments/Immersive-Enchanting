package me.alfie.immersiveenchanting.events;

import me.alfie.immersiveenchanting.events.client.ClientEvents;
import me.alfie.immersiveenchanting.events.command.CommandEvents;
import me.alfie.immersiveenchanting.events.creative.CreativeEvents;
import me.alfie.immersiveenchanting.events.datapack.DatapackEvents;
import me.alfie.immersiveenchanting.events.enchanting.EnchantingTableEvents;
import me.alfie.immersiveenchanting.events.gameplay.GameplayEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModEvents {

    public static void register(IEventBus modEventBus) {
        MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        MinecraftForge.EVENT_BUS.register(CommandEvents.class);
        MinecraftForge.EVENT_BUS.register(DatapackEvents.class);
        MinecraftForge.EVENT_BUS.register(EnchantingTableEvents.class);
        MinecraftForge.EVENT_BUS.register(GameplayEvents.class);

        modEventBus.addListener(ClientEvents::loadClientResources);
        modEventBus.addListener(ClientEvents::registerScreens);
        modEventBus.addListener(ClientEvents::registerInternalEnchantingTooltips);
        modEventBus.addListener(CreativeEvents::onBuildModCreativeTab);
        modEventBus.addListener(DatapackEvents::setDatapackClient);
    }
}
