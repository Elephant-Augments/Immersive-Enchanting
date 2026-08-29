package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.api.node.NodeTemplate;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NodeBranch extends CanvasRenderable {

    private final List<Node> nodes;
    private float angle;
    private BranchTexture texture;
    private final ResourceId id;
    @Nullable
    private NodeBranch originBranch;
    private int originNodeIndex = -1;

    /**
     * Constructor for API users - angles are automatically calculated by ImmersiveEnchanting.
     * @param canvas
     * @param nodeTemplates
     */
    public NodeBranch(Canvas canvas, ResourceId id, List<NodeTemplate> nodeTemplates) {
        super(canvas);
        this.id = id;

        List<Node> builtNodes = new ArrayList<>();
        for(NodeTemplate nodeTemplate : nodeTemplates) {
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

    /**
     * Attaches this branch as a dependency child of {@code originBranch}, branching from
     * the node at {@code originNodeIndex}.
     */
    public void attachTo(NodeBranch originBranch, int originNodeIndex) {
        this.originBranch = originBranch;
        this.originNodeIndex = originNodeIndex;
    }

    public boolean hasOrigin() {
        return originBranch != null && originNodeIndex >= 0
                && originNodeIndex < originBranch.nodes().size();
    }

    @Nullable
    public NodeBranch originBranch() {
        return originBranch;
    }

    public int originNodeIndex() {
        return originNodeIndex;
    }

    @Nullable
    public Node originNode() {
        if(!hasOrigin()) return null;
        return originBranch.nodes().get(originNodeIndex);
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

    /**
     * Positions each node along the branch's angle at equal step intervals from the canvas center
     * (or from an attached origin node), then recalculates the connector line textures between them.
     */
    public void placeNodesAlongLine(BranchSpacing spacing) {
        double dirX = Math.cos(angle);
        double dirY = Math.sin(angle);
        int nodeStep = spacing.nodeStep();

        float originX;
        float originY;
        float originRadius = 0f;
        Node origin = originNode();
        if(origin != null) {
            originX = centerX(origin);
            originY = centerY(origin);
            originRadius = nodeRadius(origin);
        } else {
            originX = canvas().getCenter().x();
            originY = canvas().getCenter().y();
        }

        for (int i = 0; i < nodes.size(); i++) {
            double radialDistance = nodeStep * (i + 1);
            if(origin != null) {
                //First child node sits one step past the parent node's border, not at its center.
                radialDistance = originRadius + nodeStep * (i + 1);
            }

            int x = (int) Math.round(originX + dirX * radialDistance);
            int y = (int) Math.round(originY + dirY * radialDistance);

            Node node = nodes().get(i);
            node.setCanvasPos(x - (float) Node.WIDTH / 2, y - (float) Node.HEIGHT / 2);
            node.setScaleKeepPos(spacing.branchScale(),
                    node.getState().getSpriteForTier(node.getTier()));
        }

        texture.calculateNodeConnections();
    }

    static float centerX(Node node) {
        return node.canvasX() + node.getScaledLength(Node.WIDTH) / 2f;
    }

    static float centerY(Node node) {
        return node.canvasY() + node.getScaledLength(Node.HEIGHT) / 2f;
    }

    static float nodeRadius(Node node) {
        return Math.max(node.getScaledLength(Node.WIDTH), node.getScaledLength(Node.HEIGHT)) / 2f;
    }

    /**
     * Point on the node border along {@code angle}.
     */
    static void borderPoint(Node node, float angle, float[] out) {
        out[0] = centerX(node) + (float) Math.cos(angle) * nodeRadius(node);
        out[1] = centerY(node) + (float) Math.sin(angle) * nodeRadius(node);
    }

    static float angleToward(float fromX, float fromY, float toX, float toY) {
        return (float) Math.atan2(toY - fromY, toX - fromX);
    }

    /**
     * Draws only the connector lines for this branch.
     */
    public void renderBranchLines(GuiGraphicsX gx, MousePos mousePos) {
        texture.render(gx, mousePos);
    }

    /**
     * Draws this branch's nodes. Must run after all {@link #renderBranchLines} passes.
     */
    public void renderNodes(GuiGraphicsX gx, MousePos mousePos) {
        for(Node node : nodes) {
            node.render(gx, mousePos);
        }
    }

    @Override
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        renderBranchLines(gx, mousePos);
        renderNodes(gx, mousePos);
    }


    public ResourceId id() {
        return id;
    }
}
