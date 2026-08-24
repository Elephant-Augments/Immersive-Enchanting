package me.alfie.immersiveenchanting.api;

import me.alfie.immersiveenchanting.api.description.RegisterDescriptionLayoutEvent;
import me.alfie.immersiveenchanting.api.description.TooltipDescriptionExtensions;
import me.alfie.immersiveenchanting.api.filter.FilterBranches;
import me.alfie.immersiveenchanting.api.filter.RegisterFilterBranchesEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

public class ApiPostEvents {
    public static void postRegisterTooltipDescriptionsEvent(FMLLoadCompleteEvent event) {
        ModLoader.postEvent(new RegisterDescriptionLayoutEvent(TooltipDescriptionExtensions::register));
    }

    public static void postRegisterFilterBranchesEvent(FMLLoadCompleteEvent event) {
        ModLoader.postEvent(new RegisterFilterBranchesEvent(
                FilterBranches::registerGlobal,
                FilterBranches::registerIsolated
        ));
    }
}
