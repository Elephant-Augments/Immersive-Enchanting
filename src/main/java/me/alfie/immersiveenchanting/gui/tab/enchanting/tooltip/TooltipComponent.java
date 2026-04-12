package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import net.minecraft.resources.Identifier;
import org.joml.Vector2i;

public abstract class TooltipComponent extends NineSliceBox {

    private Vector2i textStartPos;

    public TooltipComponent(Identifier texture) {
        super(texture);
    }

    public void setTextStartPos(int x, int y) {
        textStartPos = new Vector2i(x, y);
    }

    public Vector2i getTextStartPos() {
        return textStartPos;
    }
}
