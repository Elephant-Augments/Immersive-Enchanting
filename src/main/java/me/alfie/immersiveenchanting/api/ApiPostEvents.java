package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.api.description.RegisterDescriptionLayoutEvent;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ApiPostEvents {
    public static void postRegisterTooltipDescriptionsEvent(FMLLoadCompleteEvent event) {
        ModLoader.postEvent(new RegisterDescriptionLayoutEvent(TooltipDescriptionExtensions::register));
    }
}
