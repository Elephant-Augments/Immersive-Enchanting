package me.alfie.immersiveenchanting.api.filter;

import me.alfie.immersiveenchanting.api.filter.internal.AllModsFilterBranch;
import me.alfie.immersiveenchanting.api.filter.internal.ByModFilterBranch;
import me.alfie.immersiveenchanting.api.filter.internal.CosmeticsFilterBranch;
import me.alfie.immersiveenchanting.api.filter.internal.UnlockedFilterBranch;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;

/**
 * Fired during {@link net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent} so mods can
 * register new {@link GlobalFilterBranch}/{@link IsolatedFilterBranch} contributions.
 */
public class RegisterFilterBranchesEvent extends Event implements IModBusEvent {

    private final Consumer<GlobalFilterBranch> globalRegistrar;
    private final Consumer<IsolatedFilterBranch> isolatedRegistrar;

    public RegisterFilterBranchesEvent(
            Consumer<GlobalFilterBranch> globalRegistrar,
            Consumer<IsolatedFilterBranch> isolatedRegistrar
    ) {
        this.globalRegistrar = globalRegistrar;
        this.isolatedRegistrar = isolatedRegistrar;
    }

    public void registerGlobal(GlobalFilterBranch branch) {
        globalRegistrar.accept(branch);
    }

    public void registerIsolated(IsolatedFilterBranch branch) {
        isolatedRegistrar.accept(branch);
    }

    /** Registers IE's built-in filter branches. */
    public static void registerInternalFilterBranches(RegisterFilterBranchesEvent event) {
        event.registerGlobal(new ByModFilterBranch());
        event.registerGlobal(new AllModsFilterBranch());
        event.registerGlobal(new UnlockedFilterBranch());
        event.registerIsolated(new CosmeticsFilterBranch());
    }
}
