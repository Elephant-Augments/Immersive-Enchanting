package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.api.enchanting_tab.NodeTemplate;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class NodeBranch extends CanvasRenderable {

    private final List<Node> nodes;
    private float angle;
    private BranchTexture texture;
    private final Identifier id;

    /**
     * Constructor for API users - angles are automatically calculated by ImmersiveEnchanting.
     * @param canvas
     * @param nodeTemplates
     */
    public NodeBranch(Canvas canvas, Identifier id, List<NodeTemplate> nodeTemplates) {
        super(canvas);
        this.id = id;

        List<Node> builtNodes = new ArrayList<>();
        for(NodeTemplate nodeTemplate : nodeTemplates) {
            builtNodes.add(new Node(
                    nodeTemplate.level(),
                    canvas,
                    nodeTemplate.state(),
                    nodeTemplate.tier(),
                    nodeTemplate.type(),
                    this
            ));
        }
        nodes = builtNodes;

        texture = new BranchTexture(this, canvas);
    }

    public void setAngle(float angle) {
        this.angle = angle;
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


    public Identifier id() {
        return id;
    }
}
