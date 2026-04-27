package me.alfie.immersiveenchanting.gui.core;


public interface ScreenEventListener {

    default boolean onMouseClick(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean onMouseDrag(double mouseX, double mouseY, int button, double dx, double dy) {
        return false;
    }

    default boolean onMouseRelease(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean onMouseScrolled(double mouseX, double mouseY, double scrollY) {
        return false;
    }
}
