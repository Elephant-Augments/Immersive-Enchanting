package me.alfie.immersiveenchanting.gui.tab.enchanting;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.BranchManager;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.NodeBranch;
import net.minecraft.client.gui.GuiGraphics;

public class EnchantingTab {

    private final CentralSlot centralSlot;

    private final EnchantingTableScreen screen;
    private final BranchManager branchManager;

    public EnchantingTab(EnchantingTableScreen screen) {
        this.screen = screen;

        branchManager = new BranchManager(screen);
        centralSlot = new CentralSlot(screen.canvas());
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        for(NodeBranch branch : branchManager.branches()) {
            branch.render(graphics, mouseX, mouseY);
        }

        centralSlot.render(graphics, mouseX, mouseY);
    }

    public CentralSlot centralSlot() {
        return centralSlot;
    }

    public BranchManager branchManager() {
        return branchManager;
    }
}
