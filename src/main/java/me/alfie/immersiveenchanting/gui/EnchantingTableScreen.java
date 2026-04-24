package me.alfie.immersiveenchanting.gui;

import com.mojang.blaze3d.platform.InputConstants;
import me.alfie.immersiveenchanting.gui.tab.TabButton;
import me.alfie.immersiveenchanting.gui.tab.book.BookTab;
import me.alfie.immersiveenchanting.gui.tab.book.FilterCheckbox;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.TooltipManager;
import me.alfie.immersiveenchanting.util.FxHelper;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.CostRenderer;
import me.alfie.immersiveenchanting.gui.canvas.CanvasCamera;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        super(menu, inventory, title, 256, 222);
        registryAccess = inventory.player.registryAccess();
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
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);


        if(!canvas().DEBUG_DISABLE_CULLING) graphics.enableScissor(
                camera.VIEWPORT_X, camera.VIEWPORT_Y,
                camera.VIEWPORT_X + camera.VIEWPORT_WIDTH, camera.VIEWPORT_Y + camera.VIEWPORT_HEIGHT);

        graphics.pose().pushMatrix();
        graphics.pose().translate(camera.VIEWPORT_X, camera.VIEWPORT_Y);
        graphics.pose().scale(camera.zoom());
        graphics.pose().translate(-camera.x(), -camera.y());

        canvas().render(graphics);

        if(isState(ScreenState.ENCHANTING)) {
            enchantingTab.render(graphics, mouseX, mouseY);
        }

        graphics.pose().popMatrix();

        if(isState(ScreenState.BOOKS)) {
            bookTab.render(graphics, mouseX, mouseY);
        }

        if(!canvas().DEBUG_DISABLE_CULLING) graphics.disableScissor();

        graphics.blit(RenderPipelines.GUI_TEXTURED,
                Sprite.ENCHANTING_TABLE_GUI.id(),
                getGuiLeft(), getGuiTop(),
                0f,0f,
                imageWidth, imageHeight,
                Sprite.ENCHANTING_TABLE_GUI.width(), Sprite.ENCHANTING_TABLE_GUI.height());

        tabButton.render(graphics, mouseX, mouseY);

    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractContents(graphics, mouseX, mouseY, a);
        this.extractCarriedItem(graphics, mouseX, mouseY);
        this.extractSnapbackItem(graphics);

        List<Node> renderedNodes = enchantingTab.branchManager().getAllNodes();

        if(tooltipManager.hasActiveTooltip() && !renderedNodes.contains(tooltipManager.getActiveTooltipNode()))
            tooltipManager.clearActiveTooltip();

        if(tooltipManager.hasActiveTooltip())
            tooltipManager.getActiveTooltip().render(graphics, mouseX, mouseY);

        if(!tooltipManager.isTooltipLocked()) this.extractTooltip(graphics, mouseX, mouseY);

        if(!tooltipManager.isTooltipRequestedThisFrame() && !tooltipManager.isTooltipLocked())
            tooltipManager.clearActiveTooltip();

        tooltipManager.resetFrameState();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        this.inventoryLabelX = 16;
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    @Override
    public boolean mouseClicked(@NotNull MouseButtonEvent mouse, boolean doubleClick) {
        if(tabButton.onMouseClick(mouse)) return true;

        if(isState(ScreenState.ENCHANTING)) {
            if(enchantingTab.centralSlot().onMouseClick(mouse)) return true;

            if(tooltipManager.hasActiveTooltip() && tooltipManager.getActiveTooltip().onMouseClick(mouse)) return true;

            if(camera.onMouseClick(mouse)) return true;
        } else if(isState(ScreenState.BOOKS)) {
            if(bookTab.scrollbar().onMouseClick(mouse)) return true;

            for(FilterCheckbox checkbox : bookTab.filterCheckboxes()) if(checkbox.onMouseClick(mouse)) return true;
        }

        return super.mouseClicked(mouse, doubleClick);
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent mouse, double dx, double dy) {
        if(camera.onMouseDrag(mouse, dx / camera().zoom(), dy / camera().zoom())) return true;

        if(bookTab.scrollbar().onMouseDrag(mouse, dx, dy)) return true;

        return super.mouseDragged(mouse, dx, dy);
    }

    @Override
    public boolean mouseReleased(@NotNull MouseButtonEvent mouse) {
        if(tooltipManager().hasActiveTooltip() && tooltipManager().getActiveTooltip().onMouseRelease(mouse)) return true;

        if(camera.onMouseRelease(mouse)) return true;

        if(bookTab.scrollbar().onMouseRelease(mouse)) return true;

        return super.mouseReleased(mouse);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if(isState(ScreenState.ENCHANTING)) {
            if(camera.onMouseScrolled(x, y, scrollY)) return true;
        } else if(isState(ScreenState.BOOKS)) {
            if(bookTab.scrollbar().onMouseScrolled(x, y, scrollY)) return true;
        }

        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        if(isState(ScreenState.BOOKS)) {
            if(event.key() == InputConstants.KEY_BACKSPACE) {
                bookTab.searchbar().removeCharFromSearch();
            }

            if(event.key() == InputConstants.KEY_ESCAPE) {
                return super.keyPressed(event);
            } else {
                return true;
            }
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NotNull CharacterEvent event) {
        if(isState(ScreenState.BOOKS)) {
            bookTab.searchbar().addCharToSearch((char) event.codepoint());
        }

        return super.charTyped(event);
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
