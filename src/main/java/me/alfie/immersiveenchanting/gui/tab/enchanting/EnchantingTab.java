package me.alfie.immersiveenchanting.gui.tab.enchanting;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.*;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.TooltipManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the main "Enchanting" tab in the {@link EnchantingTableScreen}.
 *
 * <p>This tab is responsible for rendering and managing the interactive
 * enchantment graph UI, including:
 * <ul>
 *     <li>The central item slot</li>
 *     <li>All enchantment branches and nodes</li>
 * </ul>
 *
 * <p>Enchantments are displayed as a structured set of {@link NodeBranch}
 * instances managed by the {@link BranchManager}, allowing for a visual
 * progression system.</p>
 */
public class EnchantingTab {

    private final CentralSlot centralSlot;

    private final EnchantingTableScreen screen;
    private final BranchManager branchManager;

    /**
     * Constructs the enchanting tab and initializes its core components.
     *
     * <p>Initializes:
     * <ul>
     *     <li>{@link BranchManager} for managing enchantment branches</li>
     *     <li>{@link CentralSlot} for displaying the current item</li>
     * </ul>
     *
     * @param screen The parent {@link EnchantingTableScreen}
     */
    public EnchantingTab(EnchantingTableScreen screen) {
        this.screen = screen;

        branchManager = new BranchManager(screen);
        centralSlot = new CentralSlot(screen.canvas());
    }

    /**
     * Renders the enchanting tab UI.
     *
     * <p>This includes:
     * <ul>
     *     <li>All enchantment branches and their nodes</li>
     *     <li>The central item slot</li>
     * </ul>
     *
     * @param graphics Rendering context
     * @param mouseX   Current mouse X
     * @param mouseY   Current mouse Y
     */
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        for(NodeBranch branch : branchManager.branches()) {
            branch.render(graphics, mouseX, mouseY);
        }

        centralSlot.render(graphics, mouseX, mouseY);
    }

    /**
     * @return The central slot displaying the item being enchanted
     */
    public CentralSlot centralSlot() {
        return centralSlot;
    }

    /**
     * @return The {@link BranchManager} responsible for managing enchantment branches
     */
    public BranchManager branchManager() {
        return branchManager;
    }
}
