package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.ScrollableCanvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2i;

import java.util.Comparator;
import java.util.List;

public class NodeBranch extends CanvasRenderable {

    private List<Node> nodes;
    private float angle;
    private BranchTexture texture;

    public NodeBranch(ScrollableCanvas canvas, List<Node> nodes, float angle) {
        super(canvas);
        this.nodes = nodes;
        this.angle = angle;

        texture = new BranchTexture(this, canvas);
    }

    public List<Node> nodes() {
        return nodes;
    }

    public float angle() {
        return angle;
    }

    public void placeNodesAlongLine(BranchManager branchManager) {
        Vector2i centerPos = canvas().getLocalCenterPos(Node.WIDTH, Node.HEIGHT);
        double stepX = Math.cos(angle) * branchManager.getNodeStep();
        double stepY = Math.sin(angle) * branchManager.getNodeStep();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.setPos(centerPos.x + stepX * (i+1), centerPos.y + stepY * (i+1));
        }

        texture.calculateNodeConnections();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        texture.render(graphics, mouseX, mouseY);

        for(Node node : nodes) {
            node.render(graphics, mouseX, mouseY);
        }
    }


}
