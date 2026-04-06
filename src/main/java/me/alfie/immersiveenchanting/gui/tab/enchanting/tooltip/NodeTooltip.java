package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.datapack.EnchantmentUtil;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Vector2f;


public class NodeTooltip {

    private final Node node;
    private final EnchantingTableScreen screen;

    private final TooltipTitle title;
    private final TooltipDescription description;

    public NodeTooltip(EnchantingTableScreen screen, Node node) {
        this.node = node;
        this.screen = screen;

        this.title = new TooltipTitle(this);
        this.description = new TooltipDescription(this);
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Vector2f screenPos = screen.canvas().canvasToScreen(node.canvasX(), node.canvasY());

        title.setPos((int) screenPos.x(), (int) screenPos.y());
        title.setWidth(Minecraft.getInstance().font.width(title.getTitleText()));
        title.setHeight(Minecraft.getInstance().font.lineHeight);
        title.render(graphics);
    }

    public Node node() {
        return node;
    }

    public EnchantingTableScreen screen() {
        return screen;
    }
}
