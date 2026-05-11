package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeBranch;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event fired during the branch construction phase of the enchanting UI.
 * <p>
 * Allows mods or extensions to register additional {@link NodeBranch}s
 * that will be displayed on the provided {@link Canvas}.
 */
public class BuildBranchesEvent extends Event {
    private final ItemStack stack;
    private final CostRegistry costRegistry;
    private final Canvas canvas;

    private final List<NodeBranch> branches = new ArrayList<>();

    public BuildBranchesEvent(ItemStack stack, CostRegistry costRegistry, Canvas canvas) {
        this.stack = stack;
        this.costRegistry = costRegistry;
        this.canvas = canvas;
    }

    /**
     * Returns the {@link ItemStack} currently placed in the tool slot.
     *
     * @return the active item stack being processed by the enchanting UI
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Returns the client-side {@link CostRegistry} used for calculating enchantment costs.
     * <p>
     * This value is only valid on the client and must not be used for server logic.
     *
     * @return client-side cost registry
     */
    public CostRegistry costRegistry() {
        return costRegistry;
    }

    /**
     * Returns the {@link Canvas} this event is building branches for.
     *
     * @return the target canvas instance
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Returns a mutable list of all registered {@link NodeBranch}s.
     * <p>
     * Mods may modify this list directly, but prefer using {@link #addBranch(NodeBranch)}
     * for clarity.
     *
     * @return list of branches to be rendered
     */
    public List<NodeBranch> getBranches() {
        return branches;
    }

    /**
     * Adds a single {@link NodeBranch} to the canvas.
     *
     * @param branch the branch to add
     */
    public void addBranch(NodeBranch branch) {
        branches.add(branch);
    }

    /**
     * Adds multiple {@link NodeBranch}s to the canvas.
     *
     * @param branches branches to add
     */
    public void addBranches(List<NodeBranch> branches) {
        this.branches.addAll(branches);
    }
}
