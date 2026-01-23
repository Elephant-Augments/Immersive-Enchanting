package me.alfie.immersiveenchanting.gui.tooltip;

public enum RenderDirection {
    RIGHT_DOWN(false, false), //default: left to right, title on top, cost on bottom
    LEFT_DOWN(true, false), //right to left, title on top, cost on bottom
    RIGHT_UP(false, true), //left to right, cost on top, title on bottom
    LEFT_UP(true, true); //right to left, cost on top, title on bottom

    private final boolean flippedX; //Is x flipped from default (not left to right)
    private final boolean flippedY; //Is y flipped from default (not top to bottom)

    RenderDirection(boolean flippedX, boolean flippedY) {
        this.flippedX = flippedX;
        this.flippedY = flippedY;
    }

    /**
     * Is the render direction horizontally flipped (right to left instead of default left to right)?
     *
     * @return
     */
    public boolean isFlippedX() {
        return flippedX;
    }

    /**
     * Is the render direction vertically flipped (cost on top, title on bottom instead of default title on top, cost on bottom)?
     *
     * @return
     */
    public boolean isFlippedY() {
        return flippedY;
    }
}
