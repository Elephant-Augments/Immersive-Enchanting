package me.alfie.immersiveenchanting.api.description;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class DescriptionHelper {

    /**Use this offset when rendering text next to an item*/
    public static final int ITEM_Y_OFFSET = 6;

    public static void text(GuiGraphicsExtractor graphics, Component component, int x, int y) {
        graphics.text(Minecraft.getInstance().font, component, x, y, Color.WHITE.getRGB());
    }

}
