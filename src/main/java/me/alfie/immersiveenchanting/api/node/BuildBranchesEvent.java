package me.alfie.immersiveenchanting.api.node;

import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeBranch;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * NeoForge event fired on the NeoForge event bus each time the enchanting tab builds its branch list.
 *
 * <p>Internal branches (enchantments, transmute, replicate, mod-filters) are added first by
 * {@link me.alfie.immersiveenchanting.gui.tab.enchanting.node.BranchFactory}. Listeners can call
 * {@link #addBranch} / {@link #addBranches} to inject additional custom branches.
 *
 * <p>The item in the tool slot, the server-side cost registry, and the canvas are available
 * via the event getters so that listeners can tailor branches to the current context.
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
     * Get the current ItemStack in the tool slot.
     *
     * @return
     */
    public ItemStack getStack() {
        return stack;
    }

    public CostRegistry costRegistry() {
        return costRegistry;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public List<NodeBranch> getBranches() {
        return branches;
    }

    public void addBranch(NodeBranch branch) {
        branches.add(branch);
    }

    public void addBranches(List<NodeBranch> branches) {
        this.branches.addAll(branches);
    }
}
