package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeBranch;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder used to construct a {@link NodeBranch}.
 * <p>
 * A branch represents a grouped set of {@link NodeTemplate}s displayed on a {@link Canvas}.
 * This builder provides a fluent API for registering nodes before final construction.
 */
public class BranchBuilder {

    private final Canvas canvas;
    private final Identifier id;
    private final List<NodeTemplate> nodeTemplates = new ArrayList<>();

    private BranchBuilder(Canvas canvas, Identifier id) {
        this.canvas = canvas;
        this.id = id;
    }

    /**
     * Creates a new {@link BranchBuilder} for the given canvas and branch identifier.
     *
     * @param canvas the canvas this branch will be rendered on
     * @param id     unique identifier for this branch. This id is also used to play unique node sounds in {@link me.alfie.immersiveenchanting.datapack.node_sounds.NodeSoundsDatapack}
     * @return a new branch builder instance
     */
    public static BranchBuilder of(Canvas canvas, Identifier id) {
        return new BranchBuilder(canvas, id);
    }

    /**
     * Builds the final {@link NodeBranch} instance.
     *
     * @return a constructed node branch containing all added templates
     */
    public NodeBranch build() {
        return new NodeBranch(canvas, id, new ArrayList<>(nodeTemplates));
    }

    /**
     * Adds a single node template to this branch.
     *
     * @param template the node template to add
     * @return this builder for chaining
     */
    public BranchBuilder node(NodeTemplate template) {
        nodeTemplates.add(template);
        return this;
    }

    /**
     * Adds multiple node templates to this branch.
     *
     * @param templates list of node templates to add
     * @return this builder for chaining
     */
    public BranchBuilder nodes(List<NodeTemplate> templates) {
        nodeTemplates.addAll(templates);
        return this;
    }

}
