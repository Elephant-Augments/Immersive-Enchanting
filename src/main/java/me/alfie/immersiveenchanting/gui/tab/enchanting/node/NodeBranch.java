package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.api.node.NodeTemplate;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class NodeBranch extends CanvasRenderable {

    private final List<Node> nodes;
    private final ResourceLocation id;
    private float angle;
    private final BranchTexture texture;

    /**
     * Constructor for API users - angles are automatically calculated by ImmersiveEnchanting.
     *
     * @param canvas
     * @param nodeTemplates
     */
    public NodeBranch(Canvas canvas, ResourceLocation id, List<NodeTemplate> nodeTemplates) {
        super(canvas);
        this.id = id;

        List<Node> builtNodes = new ArrayList<>();
        for (NodeTemplate nodeTemplate : nodeTemplates) {
            builtNodes.add(new Node(
                    nodeTemplate.title(),
                    nodeTemplate.level(),
                    canvas,
                    nodeTemplate.state(),
                    nodeTemplate.tier(),
                    nodeTemplate.icon(),
                    this,
                    nodeTemplate.data()
            ));
        }
        nodes = builtNodes;

        texture = new BranchTexture(this, canvas);
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float angle() {
        return angle;
    }

    /**
     * Positions each node along the branch's angle at equal step intervals from the canvas center,
     * then recalculates the connector line textures between them.
     */
    public void placeNodesAlongLine() {
        double stepX = Math.cos(angle) * BranchManager.getNodeStep();
        double stepY = Math.sin(angle) * BranchManager.getNodeStep();

        for (int i = 0; i < nodes.size(); i++) {
            int x = (int) Math.round(canvas().getCenter().x() + stepX * (i + 1));
            int y = (int) Math.round(canvas().getCenter().y() + stepY * (i + 1));

            Node node = nodes().get(i);
            node.setCanvasPos(x - (float) Node.WIDTH / 2, y - (float) Node.HEIGHT / 2);
            node.setScaleKeepPos(BranchManager.getNodeBranchScale(),
                    node.getState().getSpriteForTier(node.getTier()));
        }

        texture.calculateNodeConnections();
    }

    public List<Node> nodes() {
        return nodes;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        texture.render(graphics, mouseX, mouseY);

        for (Node node : nodes) {
            node.render(graphics, mouseX, mouseY);
        }
    }


    public ResourceLocation id() {
        return id;
    }
}
