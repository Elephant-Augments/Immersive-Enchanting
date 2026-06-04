package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.api.description.RegisterDescriptionLayoutEvent;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ApiPostEvents {
    public static void postRegisterTooltipDescriptionsEvent(FMLLoadCompleteEvent event) {
        ModLoader.get().postEvent(new RegisterDescriptionLayoutEvent(TooltipDescriptionExtensions::register));
    }
}
