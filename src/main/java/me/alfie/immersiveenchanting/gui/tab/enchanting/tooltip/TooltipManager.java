package me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.networking.RemoveEnchantmentPacket;
import me.alfie.immersiveenchanting.util.EnchantmentUtil;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class TooltipManager {

    private Node lockedTooltipNode;
    private NodeTooltip activeTooltip;
    private int activeTooltipPriority;
    private boolean isTooltipRequestedThisFrame;
    private final EnchantingTableScreen screen;

    private Node heldTooltipNode;
    private long holdStartTime;
    public static final long HOLD_TRESHOLD_MILLIS = 1000;

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

    public boolean isTooltipLockedFor(Node node) {
        return lockedTooltipNode == node;
    }

    public Node getActiveTooltipNode() {
        return activeTooltip != null ? activeTooltip.node() : null;
    }

    public NodeTooltip getActiveTooltip() {
        return activeTooltip;
    }

    public boolean hasActiveTooltip() {
        return activeTooltip != null;
    }

    public boolean isActiveTooltipFor(Node node) {
        return activeTooltip != null && activeTooltip.node().equals(node);
    }

    public void requestTooltip(Node node, int priority) {
        if(priority < activeTooltipPriority) return;

        isTooltipRequestedThisFrame = true;
        setTooltip(node, priority);
    }

    public boolean isTooltipRequestedThisFrame() {
        return isTooltipRequestedThisFrame;
    }

    private void setTooltip(Node node, int priority) {
        if(isTooltipLocked()) return;
        if(hasActiveTooltip() && getActiveTooltipNode().equals(node)) return;

        activeTooltip = new NodeTooltip(screen, node);
        activeTooltipPriority = priority;
        FxHelper.playNodeHover(screen.player().level(), node);
    }

    public void resetFrameState() {
        isTooltipRequestedThisFrame = false;
    }

    public void clearActiveTooltip() {
        activeTooltip = null;
        activeTooltipPriority = 0;
    }


    public void startHold(Node node) {
        heldTooltipNode = node;
        holdStartTime = System.currentTimeMillis();
    }

    public void resetHold() {
        heldTooltipNode = null;
        holdStartTime = -1;
    }

    public boolean isHoldingTooltip() {
        return heldTooltipNode != null;
    }

    public void updateHold() {
        if(heldTooltipNode == null) return;

        if(getElapsedHeldTime() >= HOLD_TRESHOLD_MILLIS) triggerHeldTooltip();
    }

    public long getElapsedHeldTime() {
        return System.currentTimeMillis() - holdStartTime;
    }

    private void triggerHeldTooltip() {
        ClientPacketDistributor.sendToServer(new RemoveEnchantmentPacket(
                EnchantmentUtil.toHolder(heldTooltipNode.branchId(), screen.registryAccess()),
                heldTooltipNode.getPosition()));

        resetHold();
    }
}
