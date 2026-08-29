package me.alfie.immersiveenchanting.api.node;

import me.alfie.alfinolib.util.ResourceId;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.branch.NodeBranch;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for constructing a {@link me.alfie.immersiveenchanting.gui.tab.enchanting.branch.NodeBranch}
 * from a sequence of {@link NodeTemplate} descriptors.
 *
 * <p>Use {@link #of} to create an instance, chain {@link #node}/{@link #nodes} calls to add
 * node templates in order, then call {@link #build} to produce the branch.
 */
public class BranchBuilder {

    private final Canvas canvas;
    private final ResourceId id;
    private final List<NodeTemplate> nodeTemplates = new ArrayList<>();
    @Nullable
    private NodeBranch originBranch;
    private int originNodeIndex = -1;

    private BranchBuilder(Canvas canvas, ResourceId id) {
        this.canvas = canvas;
        this.id = id;
    }

    public static BranchBuilder of(Canvas canvas, ResourceId id) {
        return new BranchBuilder(canvas, id);
    }

    /**
     * Attaches this branch as a dependency child of an {@code originBranch}, branching from
     * the node at {@code originNodeIndex}. Child nodes are positioned outward from that
     * parent node instead of from the canvas center.
     */
    public BranchBuilder attachTo(NodeBranch originBranch, int originNodeIndex) {
        this.originBranch = originBranch;
        this.originNodeIndex = originNodeIndex;
        return this;
    }

    public NodeBranch build() {
        NodeBranch branch = new NodeBranch(canvas, id, new ArrayList<>(nodeTemplates));
        if(originBranch != null && originNodeIndex >= 0) {
            branch.attachTo(originBranch, originNodeIndex);
        }
        return branch;
    }

    public BranchBuilder node(NodeTemplate template) {
        nodeTemplates.add(template);
        return this;
    }

    public BranchBuilder nodes(List<NodeTemplate> templates) {
        nodeTemplates.addAll(templates);
        return this;
    }

}
