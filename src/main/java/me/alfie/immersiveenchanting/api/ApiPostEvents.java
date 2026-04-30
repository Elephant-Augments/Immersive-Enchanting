package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.api.description.RegisterDescriptionLayoutEvent;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.api.node.RegisterNodeClickHandlerEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ApiPostEvents {
    public static void postRegisterTooltipDescriptionsEvent(FMLLoadCompleteEvent event) {
        NeoForge.EVENT_BUS.post(new RegisterDescriptionLayoutEvent(TooltipDescriptionExtensions::register));
    }

    public static void postRegisterNodeClickHandlersEvent(FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.post(new RegisterNodeClickHandlerEvent());
    }
}
