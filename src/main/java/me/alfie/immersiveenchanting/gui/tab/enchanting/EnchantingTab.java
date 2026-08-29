package me.alfie.immersiveenchanting.gui.tab.enchanting;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.branch.BranchManager;
import me.alfie.immersiveenchanting.gui.tab.enchanting.branch.NodeBranch;

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

    public enum Display {
        ENCHANTMENTS,
        MOD_FILTERS
    }

    private final CentralSlot centralSlot;

    private final EnchantingTableScreen screen;
    private final BranchManager branchManager;
    private Display currentDisplay = Display.ENCHANTMENTS;


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
     */
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        // Render all branch lines first and then nodes on top to avoid 
        // lines from child nodes rendering on top of parent nodes.
        for(NodeBranch branch : branchManager.branches()) {
            branch.renderBranchLines(gx, mousePos);
        }
        for(NodeBranch branch : branchManager.branches()) {
            branch.renderNodes(gx, mousePos);
        }

        centralSlot.render(gx, mousePos);
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

    public void setDisplay(Display display) {
        currentDisplay = display;
    }

    public Display getDisplay() {
        return currentDisplay;
    }

    public boolean isDisplay(Display display) {
        return currentDisplay == display;
    }
}
