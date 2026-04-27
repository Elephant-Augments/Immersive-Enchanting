package me.alfie.immersiveenchanting.api.enchanting_tab;

import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeBranch;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BranchBuilder {

    private final Canvas canvas;
    private final Identifier id;
    private final List<Node> nodes = new ArrayList<>();

    private BranchBuilder(Canvas canvas, Identifier id) {
        this.canvas = canvas;
        this.id = id;
    }

    public static BranchBuilder of(Canvas canvas, Identifier id) {
        return new BranchBuilder(canvas, id);
    }

    public NodeBranch build() {
        return new NodeBranch(canvas, new ArrayList<>(nodes));
    }

    public BranchBuilder node(NodeTemplate template) {
        nodes.add(new Node(
                id,
                template.level(),
                canvas,
                template.state(),
                template.tier(),
                template.type()
        ));
        return this;
    }

    public BranchBuilder nodes(List<NodeTemplate> templates) {
        for(NodeTemplate template : templates) {
            node(template);
        }
        return this;
    }


}
