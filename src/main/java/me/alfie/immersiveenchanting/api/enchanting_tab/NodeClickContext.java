package me.alfie.immersiveenchanting.api.enchanting_tab;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import net.minecraft.client.input.MouseButtonEvent;

import java.awt.event.InputEvent;

public record NodeClickContext(
        Node node,
        EnchantingTableScreen screen) {
}
