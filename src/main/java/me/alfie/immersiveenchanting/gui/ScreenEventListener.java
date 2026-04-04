package me.alfie.immersiveenchanting.gui;

import net.minecraft.client.input.MouseButtonEvent;

public interface ScreenEventListener {

    default boolean onMouseClick(MouseButtonEvent mouse) {
        return false;
    }

    default boolean onMouseDrag(MouseButtonEvent event, double dx, double dy) {
        return false;
    }

    default boolean onMouseRelease(MouseButtonEvent mouse) {
        return false;
    }
}
