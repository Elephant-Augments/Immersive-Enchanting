package me.alfie.immersiveenchanting.gui.core;

import net.minecraft.client.input.MouseButtonEvent;

public interface ScreenEventListener {

    default boolean onMouseClick(MouseButtonEvent mouse) {
        return false;
    }

    default boolean onMouseDrag(MouseButtonEvent mouse, double dx, double dy) {
        return false;
    }

    default boolean onMouseRelease(MouseButtonEvent mouse) {
        return false;
    }

    default boolean onMouseScrolled(double mouseX, double mouseY, double scrollY) {
        return false;
    }
}
