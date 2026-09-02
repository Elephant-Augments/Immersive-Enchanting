package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

/**
 * Pixel spacing metrics computed from root-branch angles before nodes are positioned.
 *
 * @param trunkStep distance from the canvas center to the first node in a branch
 * @param continuationStep distance between subsequent nodes
 * @param branchScale uniform node scale for the tree
 */
public record BranchSpacing(int trunkStep, int continuationStep, float branchScale) {

    /** @deprecated use {@link #continuationStep()} */
    @Deprecated
    public int nodeStep() {
        return continuationStep;
    }
}
