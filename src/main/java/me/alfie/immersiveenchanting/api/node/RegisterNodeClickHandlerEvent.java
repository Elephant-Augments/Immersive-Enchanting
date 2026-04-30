package me.alfie.immersiveenchanting.api.node;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;

public class RegisterNodeClickHandlerEvent extends Event {

    public void register(Identifier id, NodeClickHandler handler) {
        NodeClickHandlerRegistry.register(id, handler);
    }



}
