package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class NodeBranch extends CanvasRenderable {

    private List<Node> nodes;
    private float angle;
    private BranchTexture texture;

    public NodeBranch(Canvas canvas, List<Node> nodes, float angle) {
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

    public void placeNodesAlongLine() {
        double stepX = Math.cos(angle) * BranchManager.getNodeStep();
        double stepY = Math.sin(angle) * BranchManager.getNodeStep();

        for (int i = 0; i < nodes.size(); i++) {
            int x = (int) Math.round(canvas().getCenter().x() + stepX * (i+1));
            int y = (int) Math.round(canvas().getCenter().y() + stepY * (i+1));

            Node node = nodes().get(i);
            node.setCanvasPos(x - (float) Node.WIDTH / 2, y - (float) Node.HEIGHT / 2);
            node.setScaleKeepPos(BranchManager.getNodeBranchScale(),
                    node.getState().getSpriteForTier(node.getTier()));
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
