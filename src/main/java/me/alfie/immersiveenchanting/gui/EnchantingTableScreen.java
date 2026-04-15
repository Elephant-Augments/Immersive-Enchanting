package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.FxHelper;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.manager.ClientCostManager;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.CostRenderer;
import me.alfie.immersiveenchanting.gui.canvas.CanvasCamera;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EnchantingTableScreen extends AbstractContainerScreen<EnchantingTableMenu> {

    private static final Logger log = LogManager.getLogger(EnchantingTableScreen.class);
    private CanvasCamera camera;
    private final Canvas scrollableCanvas;
    private ScreenState screenState;

    private final EnchantingTab enchantingTab;

    private ItemStack lastToolSlotStack = ItemStack.EMPTY;

    private final RegistryAccess registryAccess;

    private NodeTooltip nextNodeTooltip;
    private NodeTooltip lastActiveNodeTooltip;
    private NodeTooltip activeNodeTooltip;
    private Node lockedTooltipNode;
    private final CostRenderer enchantmentCostRenderer;
    private List<Holder<Enchantment>> availableEnchantments = new ArrayList<>();

    private final Player player;

    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 222);
        registryAccess = inventory.player.registryAccess();
        this.player = inventory.player;
        this.scrollableCanvas = new Canvas(this);
        setState(ScreenState.ENCHANTING);

        enchantingTab = new EnchantingTab(this);

        this.enchantmentCostRenderer = new CostRenderer(CostRegistry.client());

        onToolSlotUpdate(ItemStack.EMPTY);


    }

    @Override
    protected void init() {
        super.init();
        this.camera = new CanvasCamera(this,getGuiLeft()+4, getGuiTop()+4);
        camera.centerCameraOnCanvas();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);


        if(!canvas().DEBUG_DISABLE_CULLING) graphics.enableScissor(
                camera.VIEWPORT_X, camera.VIEWPORT_Y,
                camera.VIEWPORT_X + camera.VIEWPORT_WIDTH, camera.VIEWPORT_Y + camera.VIEWPORT_HEIGHT);

        graphics.pose().pushMatrix();
        graphics.pose().translate(camera.VIEWPORT_X, camera.VIEWPORT_Y);
        graphics.pose().scale(camera.zoom());
        graphics.pose().translate(-camera.x(), -camera.y());

        canvas().render(graphics);
        enchantingTab.render(graphics, mouseX, mouseY);

        graphics.pose().popMatrix();

        if(!canvas().DEBUG_DISABLE_CULLING) graphics.disableScissor();

        graphics.blit(RenderPipelines.GUI_TEXTURED,
                Sprite.ENCHANTING_TABLE_GUI.id(),
                getGuiLeft(), getGuiTop(),
                0f,0f,
                imageWidth, imageHeight,
                Sprite.ENCHANTING_TABLE_GUI.width(), Sprite.ENCHANTING_TABLE_GUI.height());

    }

    public void setAvailableEnchantments(List<Holder<Enchantment>> availableEnchantments) {
        this.availableEnchantments.clear();
        this.availableEnchantments.addAll(availableEnchantments);
    }

    public List<Holder<Enchantment>> getAvailableEnchantments() {
        return availableEnchantments;
    }

    public void lockTooltip(Node node) {
        lockedTooltipNode = node;
    }

    public void unlockTooltip() {
        lockedTooltipNode = null;
    }

    public boolean isTooltipLocked(Node node) {
        return lockedTooltipNode == node;
    }

    public boolean isTooltipLocked() {
        return lockedTooltipNode != null;
    }

    public void setNextNodeTooltip(Node node) {
        nextNodeTooltip = new NodeTooltip(this, node);
    }

    public boolean isNextNodeTooltip(Node node) {
        return nextNodeTooltip.node().equals(node);
    }

    public boolean hasNextNodeTooltip() {
        return nextNodeTooltip != null;
    }

    public Node getActiveNodeTooltipNode() {
        return activeNodeTooltip.node();
    }

    public boolean hasActiveNodeTooltip() {
        return activeNodeTooltip != null;
    }

    public void requestNodeTooltip(Node node) {
        setNodeTooltip(node);
    }

    public void setNodeTooltip(Node node) {
        if(activeNodeTooltip == null || !activeNodeTooltip.node().equals(node)) {
            activeNodeTooltip = new NodeTooltip(this, node);

            FxHelper.playNodeHover(player().level(), node);
        }
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractContents(graphics, mouseX, mouseY, a);
        this.extractCarriedItem(graphics, mouseX, mouseY);
        this.extractSnapbackItem(graphics);

        if(hasNextNodeTooltip()) {
            activeNodeTooltip = nextNodeTooltip;
            nextNodeTooltip = null;
        } else {
            activeNodeTooltip = null;
            lastActiveNodeTooltip = null;
        }

        if(activeNodeTooltip != null) {
            activeNodeTooltip.render(graphics, mouseX, mouseY);

            if(lastActiveNodeTooltip == null || !lastActiveNodeTooltip.node().equals(activeNodeTooltip.node())) {
                FxHelper.playNodeHover(player().level(), activeNodeTooltip.node());
            }

            lastActiveNodeTooltip = activeNodeTooltip;
        }

        if(!isTooltipLocked()) {
            this.extractTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        this.inventoryLabelX = 16;
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouse, boolean doubleClick) {
        if(isState(ScreenState.ENCHANTING)) {
            if(enchantingTab.centralSlot().onMouseClick(mouse)) return true;

            if(activeNodeTooltip != null && activeNodeTooltip.onMouseClick(mouse)) return true;

            if(camera.onMouseClick(mouse)) return true;
        }

        return super.mouseClicked(mouse, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouse, double dx, double dy) {
        if(camera.onMouseDrag(mouse, dx / camera().zoom(), dy / camera().zoom())) return true;

        return super.mouseDragged(mouse, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouse) {
        if(camera.onMouseRelease(mouse)) return true;

        return super.mouseReleased(mouse);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if(camera.onMouseScrolled(x, y, scrollY)) return true;

        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    /**
     * Checks if the mouse is over a rectangle.
     *
     * @param x      top-left x of the rectangle
     * @param y      top-left y of the rectangle
     * @param width  width of the rectangle
     * @param height height of the rectangle
     * @param mouseX current mouse x
     * @param mouseY current mouse y
     * @return true if mouse is inside the rectangle
     */
    public boolean isMouseOver(float x, float y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }





    @Override
    protected void containerTick() {
        //Check for tool slot updates
        ItemStack stack = getMenu().getToolSlot().getItem();
        if(!ItemStack.isSameItemSameComponents(stack, lastToolSlotStack)) {
            onToolSlotUpdate(stack);
            lastToolSlotStack = stack.copy();
        }
    }

    private void onToolSlotUpdate(ItemStack newStack) {
        enchantingTab.branchManager().buildBranches(newStack);
        canvas().setSizeToFitNodes(CostRegistry.client().getHighestLevel());
        enchantingTab.branchManager().positionBranches();

        FxHelper.playToolSlotChanged(player().level());

        if(camera() == null) return;
        camera().setDraggingEnabled(!newStack.isEmpty());
        camera().setZoom(1f);
        camera().centerCameraOnCanvas();
    }


    public void setState(ScreenState state) {
        this.screenState = state;
    }

    public boolean isState(ScreenState state) {
        return this.screenState == state;
    }

    public Canvas canvas() {
        return scrollableCanvas;
    }

    public CanvasCamera camera() {
        return camera;
    }

    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    public Player player() {
        return player;
    }

    public CostRenderer enchantmentCostRenderer() {
        return enchantmentCostRenderer;
    }
}
