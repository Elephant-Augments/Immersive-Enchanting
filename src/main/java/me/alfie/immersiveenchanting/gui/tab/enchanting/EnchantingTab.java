package me.alfie.immersiveenchanting.gui.tab.enchanting;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnchantingTab {

    private final CentralSlot centralSlot;

    private final EnchantingTableScreen screen;
    private final BranchManager branchManager;

    public EnchantingTab(EnchantingTableScreen screen) {
        this.screen = screen;

        branchManager = new BranchManager(screen);
        centralSlot = new CentralSlot(screen.canvas());
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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
