package me.alfie.immersiveenchanting.api.description;


import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.function.Consumer;

public class RegisterDescriptionLayoutEvent extends Event implements IModBusEvent {

    private final Consumer<DescriptionLayoutExtension> registrar;

    public RegisterDescriptionLayoutEvent(Consumer<DescriptionLayoutExtension> registrar) {
        this.registrar = registrar;
    }

    public void register(DescriptionLayoutExtension extension) {
        registrar.accept(extension);
    }
}
