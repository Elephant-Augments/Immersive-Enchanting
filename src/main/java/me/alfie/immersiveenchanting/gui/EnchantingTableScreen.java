package me.alfie.immersiveenchanting.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasCamera;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.TabButton;
import me.alfie.immersiveenchanting.gui.tab.book.BookTab;
import me.alfie.immersiveenchanting.gui.tab.book.FilterCheckbox;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.CostRenderer;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.TooltipManager;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnchantingTableScreen extends AbstractContainerScreen<@NotNull EnchantingTableMenu> {

    private static final Logger log = LogManager.getLogger(EnchantingTableScreen.class);
    private CanvasCamera camera;
    private final Canvas scrollableCanvas;
    private ScreenState screenState;

    private final EnchantingTab enchantingTab;
    private final BookTab bookTab;
    private final TabButton tabButton;

    private ItemStack lastToolSlotStack = ItemStack.EMPTY;

    private final RegistryAccess registryAccess;

    private final CostRenderer enchantmentCostRenderer;
    private final TooltipManager tooltipManager;


    private final Player player;

    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 222;
        this.imageWidth = 256;

        registryAccess = inventory.player.level().registryAccess();
        this.player = inventory.player;
        this.scrollableCanvas = new Canvas(this);
        setState(ScreenState.ENCHANTING);

        enchantingTab = new EnchantingTab(this);
        bookTab = new BookTab(this);
        tabButton = new TabButton(this);

        this.enchantmentCostRenderer = new CostRenderer(CostRegistry.client());
        this.tooltipManager = new TooltipManager(this);

        onToolSlotUpdate(ItemStack.EMPTY);
    }

    @Override
    protected void init() {
        super.init();
        this.camera = new CanvasCamera(this,getGuiLeft()+4, getGuiTop()+4);
        camera.centerCameraOnCanvas();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if(!canvas().DEBUG_DISABLE_CULLING) graphics.enableScissor(
                camera.VIEWPORT_X, camera.VIEWPORT_Y,
                camera.VIEWPORT_X + camera.VIEWPORT_WIDTH, camera.VIEWPORT_Y + camera.VIEWPORT_HEIGHT);

        graphics.pose().pushPose();
        graphics.pose().translate(camera.VIEWPORT_X, camera.VIEWPORT_Y, 1);
        graphics.pose().scale(camera.zoom(), camera.zoom(), 1);
        graphics.pose().translate(-camera.x(), -camera.y(), 1);

        canvas().render(graphics);

        if(isState(ScreenState.ENCHANTING)) {
            enchantingTab.render(graphics, mouseX, mouseY);
            //Scissor is disabled during CentralSlot render for hover tooltip!
        }

        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 2);
        if(isState(ScreenState.BOOKS)) {
            bookTab.render(graphics, mouseX, mouseY);
        }
        graphics.pose().popPose();


        if(!canvas().DEBUG_DISABLE_CULLING) graphics.disableScissor();

        RenderSystem.enableBlend();
        graphics.blit(
                Sprite.ENCHANTING_TABLE_GUI.id(),
                getGuiLeft(), getGuiTop(),
                0f,0f,
                imageWidth, imageHeight,
                Sprite.ENCHANTING_TABLE_GUI.width(), Sprite.ENCHANTING_TABLE_GUI.height());
        RenderSystem.disableBlend();

        tabButton.render(graphics, mouseX, mouseY);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        List<Node> renderedNodes = enchantingTab.branchManager().getAllNodes();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        if(tooltipManager.hasActiveTooltip() && !renderedNodes.contains(tooltipManager.getActiveTooltipNode()))
            tooltipManager.clearActiveTooltip();

        if(tooltipManager.hasActiveTooltip())
            tooltipManager.getActiveTooltip().render(graphics, mouseX, mouseY);

        if(!tooltipManager.isTooltipLocked()) this.renderTooltip(graphics, mouseX, mouseY);

        if(!tooltipManager.isTooltipRequestedThisFrame() && !tooltipManager.isTooltipLocked())
            tooltipManager.clearActiveTooltip();

        tooltipManager.resetFrameState();
        graphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        this.inventoryLabelX = 16;
        this.inventoryLabelY = 128;
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(tabButton.onMouseClick(mouseX, mouseY, button)) return true;

        if(isState(ScreenState.ENCHANTING)) {
            if(enchantingTab.centralSlot().onMouseClick(mouseX, mouseY, button)) return true;

            if(tooltipManager.hasActiveTooltip() && tooltipManager.getActiveTooltip().onMouseClick(mouseX, mouseY, button)) return true;

            if(camera.onMouseClick(mouseX, mouseY, button)) return true;
        } else if(isState(ScreenState.BOOKS)) {
            if(bookTab.scrollbar().onMouseClick(mouseX, mouseY, button)) return true;

            for(FilterCheckbox checkbox : bookTab.filterCheckboxes()) if(checkbox.onMouseClick(mouseX, mouseY, button)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if(camera.onMouseDrag(mouseX, mouseY, button, dragX / camera().zoom(), dragY / camera().zoom())) return true;

        if(bookTab.scrollbar().onMouseDrag(mouseX, mouseY, button, dragX, dragY)) return true;

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(tooltipManager().hasActiveTooltip() && tooltipManager().getActiveTooltip().onMouseRelease(mouseX, mouseY, button)) return true;

        if(camera.onMouseRelease(mouseX, mouseY, button)) return true;

        if(bookTab.scrollbar().onMouseRelease(mouseX, mouseY, button)) return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollY) {
        if(isState(ScreenState.ENCHANTING)) {
            if(camera.onMouseScrolled(x, y, scrollY)) return true;
        } else if(isState(ScreenState.BOOKS)) {
            if(bookTab.scrollbar().onMouseScrolled(x, y, scrollY)) return true;
        }

        return super.mouseScrolled(x, y, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(isState(ScreenState.BOOKS)) {
            if(keyCode == InputConstants.KEY_BACKSPACE) {
                bookTab.searchbar().removeCharFromSearch();
            }

            if(keyCode == InputConstants.KEY_ESCAPE) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            } else {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(isState(ScreenState.BOOKS)) {
            bookTab.searchbar().addCharToSearch(codePoint);
        }

        return super.charTyped(codePoint, modifiers);
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
    public boolean isMouseOver(double x, double y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void containerTick() {
        ItemStack stack = getMenu().getToolSlot().getItem();
        if(!ItemStack.isSameItemSameTags(stack, lastToolSlotStack)) {
            onToolSlotUpdate(stack);
            lastToolSlotStack = stack.copy();
        }
    }

    private void onToolSlotUpdate(ItemStack newStack) {
        enchantingTab.branchManager().buildBranches(newStack);
        canvas().setSizeToFitNodes(CostRegistry.client().getHighestLevel());
        enchantingTab.branchManager().positionBranches();

        FxHelper.playToolSlotChanged(player());

        if(camera() == null) return;
        if(newStack.getItem().equals(lastToolSlotStack.getItem())) return;
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

    public TooltipManager tooltipManager() {
        return tooltipManager;
    }

    public BookTab bookTab() {
        return bookTab;
    }
}
