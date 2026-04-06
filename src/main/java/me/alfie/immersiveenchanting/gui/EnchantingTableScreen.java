package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.datapack.EnchantmentCostRegistry;
import me.alfie.immersiveenchanting.gui.canvas.CanvasCamera;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.NodeTooltip;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class EnchantingTableScreen extends AbstractContainerScreen<EnchantingTableMenu> {

    private CanvasCamera camera;
    private final Canvas scrollableCanvas;
    private ScreenState screenState;

    private final EnchantingTab enchantingTab;

    private ItemStack lastToolSlotStack = ItemStack.EMPTY;

    private final RegistryAccess registryAccess;

    private NodeTooltip nextNodeTooltip;

    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 222);
        registryAccess = inventory.player.registryAccess();


        this.scrollableCanvas = new Canvas(this);
        setState(ScreenState.ENCHANTING);

        enchantingTab = new EnchantingTab(this);

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

        if(nextNodeTooltip != null) {
            nextNodeTooltip.render(graphics, mouseX, mouseY);
            nextNodeTooltip = null;
        }
    }

    public void setNextNodeTooltip(Node node) {
        nextNodeTooltip = new NodeTooltip(this, node);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouse, boolean doubleClick) {
        if(isState(ScreenState.ENCHANTING)) {
            if(enchantingTab.centralSlot().onMouseClick(mouse)) return true;

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
        canvas().setSizeToFitNodes(EnchantmentCostRegistry.getHighestLevel());
        enchantingTab.branchManager().positionBranches();

        if(camera() == null) return;
        if(newStack.isEmpty()) {
            camera().setDraggingEnabled(false);
            camera().setZoom(1f);
            camera().centerCameraOnCanvas();
        } else {
            camera().setDraggingEnabled(true);
        }




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
}
