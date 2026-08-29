package me.alfie.immersiveenchanting.gui.tab.enchanting.branch;

import me.alfie.immersiveenchanting.api.node.BuildBranchesEvent;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

/**
 * Coordinates default branch content, mod extensibility, and angular layout.
 */
public class BranchFactory {

    /**
     * Builds the full set of tree branches for the given tool stack, posts {@link BuildBranchesEvent},
     * then assigns angles via {@link BranchLayout}.
     */
    public static List<NodeBranch> buildBranches(ItemStack stack, CostRegistry costRegistry, Canvas canvas) {
        BuildBranchesEvent event = new BuildBranchesEvent(stack, costRegistry, canvas);

        addDefaultBranches(event);

        NeoForge.EVENT_BUS.post(event);

        List<NodeBranch> branches = event.getBranches();
        EnchantingTab.Display display = event.getCanvas().screen().enchantingTab().getDisplay();
        BranchLayout.assignAngles(branches, display);

        return branches;
    }

    private static void addDefaultBranches(BuildBranchesEvent event) {
        if(event.getCanvas().screen().enchantingTab().isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            EnchantmentTree.addBranches(event);
        } else if(event.getStack().is(ModItems.ANCIENT_BOOK.get())) {
            AncientBookTree.addAncientBookBranches(event);
        } else {
            EnchantmentTree.addBranches(event);
        }
    }
}
