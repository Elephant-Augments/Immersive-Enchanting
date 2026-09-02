package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the live branch tree for one enchanting screen session.
 */
public class BranchManager {

    public static final int MAX_NODE_STEP = BranchLayout.MAX_NODE_STEP;

    private final List<NodeBranch> cachedBranches = new ArrayList<>();
    private final EnchantingTableScreen screen;
    private BranchSpacing spacing = new BranchSpacing(
            BranchLayout.MIN_NODE_STEP,
            BranchLayout.MIN_NODE_STEP,
            Node.DEFAULT_SCALE
    );
    private BranchLayout.StandaloneRootPlacement standaloneRootPlacement = new BranchLayout.StandaloneRootPlacement(
            BranchLayout.STANDALONE_ROOT_INNER_RADIUS_SCALE
    );

    public BranchManager(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public List<NodeBranch> branches() {
        return cachedBranches;
    }

    public BranchSpacing spacing() {
        return spacing;
    }

    public BranchLayout.StandaloneRootPlacement standaloneRootPlacement() {
        return standaloneRootPlacement;
    }

    public List<Node> getAllNodes() {
        List<Node> result = new ArrayList<>();
        for(NodeBranch branch : branches()) {
            result.addAll(branch.nodes());
        }

        return result;
    }

    /**
     * You must call {@link #buildBranches(ItemStack)} before {@link #positionBranches()}!
     */
    public void buildBranches(ItemStack stack) {
        cachedBranches.clear();
        cachedBranches.addAll(BranchFactory.buildBranches(stack, CostRegistry.client(), screen.canvas()));
        spacing = BranchLayout.computeSpacing(cachedBranches);
        standaloneRootPlacement = BranchLayout.refineStandaloneRootAngles(cachedBranches, spacing);
    }

    /**
     * You must call {@link #buildBranches(ItemStack)} before {@link #positionBranches()}!
     */
    public void positionBranches() {
        BranchLayout.positionAll(cachedBranches, spacing);
    }
}
