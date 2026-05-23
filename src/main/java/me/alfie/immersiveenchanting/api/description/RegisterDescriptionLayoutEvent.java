package me.alfie.immersiveenchanting.api.description;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.function.Consumer;

/**
 * Fired during {@link net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent} to allow other mods
 * to register additional {@link DescriptionLayoutExtension} instances for tooltip rendering.
 *
 * <p>This event is intended as a public extension point for the tooltip description system.
 * Mods should use {@link #register(DescriptionLayoutExtension)} to add their custom extensions.</p>
 */
public class RegisterDescriptionLayoutEvent extends Event implements IModBusEvent {

    private final Consumer<DescriptionLayoutExtension> registrar;

    public RegisterDescriptionLayoutEvent(Consumer<DescriptionLayoutExtension> registrar) {
        this.registrar = registrar;
    }

    public void register(DescriptionLayoutExtension extension) {
        registrar.accept(extension);
    }
}
