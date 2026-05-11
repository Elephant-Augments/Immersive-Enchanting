package me.alfie.immersiveenchanting.gui.tab.enchanting.node;

import me.alfie.immersiveenchanting.api.node.NodeTemplate;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a branch of nodes within the enchanting UI.
 * <p>
 * A {@code NodeBranch} is a collection of {@link Node}s arranged in a radial layout
 * on a {@link Canvas}. It is responsible for:
 * <ul>
 *     <li>Constructing nodes from {@link NodeTemplate}s</li>
 *     <li>Positioning nodes along a calculated angle</li>
 *     <li>Rendering node connections and contained nodes</li>
 * </ul>
 * <p>
 * Branch layout (angle and spacing) is controlled internally by the system and
 * automatically calculated by ImmersiveEnchanting.
 */
public class NodeBranch extends CanvasRenderable {

    private final List<Node> nodes;
    private float angle;
    private BranchTexture texture;
    private final Identifier id;

    /**
     * Creates a new node branch from a set of templates.
     *
     * <p>This constructor is intended for internal use and advanced API scenarios.</p>
     *
     * <p><b>Recommended usage:</b> External code should use {@link me.alfie.immersiveenchanting.api.node.BranchBuilder}
     * instead of constructing {@code NodeBranch} directly.</p>
     *
     * Branches should be built during {@link me.alfie.immersiveenchanting.api.node.BuildBranchesEvent}
     *
     * <p>Node positioning and layout are handled automatically by the system.</p>
     *
     * @param canvas        the canvas this branch is rendered on
     * @param id            unique identifier for this branch
     * @param nodeTemplates list of node definitions used to construct the branch
     */
    public NodeBranch(Canvas canvas, Identifier id, List<NodeTemplate> nodeTemplates) {
        super(canvas);
        this.id = id;

        List<Node> builtNodes = new ArrayList<>();
        for(NodeTemplate nodeTemplate : nodeTemplates) {
            builtNodes.add(new Node(
                    canvas, this, nodeTemplate.position(), nodeTemplate.title(),
                    nodeTemplate.state(),
                    nodeTemplate.tier(),
                    nodeTemplate.icon(),
                    nodeTemplate.data()
            ));
        }
        nodes = builtNodes;

        texture = new BranchTexture(this, canvas);
    }

    /**
     * Sets the branch angle used for layout calculation.
     *
     * @param angle angle in radians
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * Returns all nodes contained in this branch.
     *
     * @return list of nodes
     */
    public List<Node> nodes() {
        return nodes;
    }

    /**
     * Returns the current branch angle used for node layout.
     *
     * @return angle in radians
     */
    public float angle() {
        return angle;
    }

    /**
     * Positions all nodes along a line based on the current branch angle.
     * <p>
     * Node spacing is determined by {@link BranchManager#getNodeStep()} and all
     * nodes are centered relative to the canvas origin.
     */
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

    /**
     * Renders the branch, including its connection texture and all contained nodes.
     */
    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        texture.render(graphics, mouseX, mouseY);

        for(Node node : nodes) {
            node.render(graphics, mouseX, mouseY);
        }
    }

    /**
     * Returns the unique identifier of this branch.
     *
     * @return branch identifier
     */
    public Identifier id() {
        return id;
    }
}
