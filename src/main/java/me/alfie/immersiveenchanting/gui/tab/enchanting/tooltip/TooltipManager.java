package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.gui.screens.Screen;

public class TooltipManager {

    private Node lockedTooltipNode;
    private NodeTooltip activeNodeTooltip;
    private boolean nodeTooltipRequestedThisFrame;
    private final EnchantingTableScreen screen;

    public TooltipManager(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public void lockTooltip(Node node) {
        lockedTooltipNode = node;
    }

    public void unlockTooltip() {
        lockedTooltipNode = null;
    }

    public boolean isTooltipLocked() {
        return lockedTooltipNode != null;
    }

    public boolean isTooltipLocked(Node node) {
        return lockedTooltipNode == node;
    }

    public Node getActiveNodeTooltipNode() {
        return activeNodeTooltip != null ? activeNodeTooltip.node() : null;
    }

    public boolean hasActiveNodeTooltip() {
        return activeNodeTooltip != null;
    }

    public boolean isActiveNodeTooltipNode(Node node) {
        return activeNodeTooltip != null && activeNodeTooltip.node().equals(node);
    }

    public void requestNodeTooltip(Node node) {
        nodeTooltipRequestedThisFrame = true;
        setNodeTooltip(node);
    }

    private void setNodeTooltip(Node node) {
        if (shouldShowTooltip(node)) {
            activeNodeTooltip = new NodeTooltip(this, node);
            FxHelper.playNodeHover(screen.player().level(), node);
        }
    }

    private boolean shouldShowTooltip(Node node) {
        if (isTooltipLocked()) return false;

        return activeNodeTooltip == null
                || !activeNodeTooltip.node().equals(node);
    }

    public void resetFrameState() {
        nodeTooltipRequestedThisFrame = false;
    }

}
