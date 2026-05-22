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
import me.alfie.immersiveenchanting.item.ModItems;
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

/**
 * Custom enchanting table screen handling both the enchanting and book browsing interfaces.
 *
 * <p>Manages rendering, input handling, and state switching between multiple UI modes,
 * including the enchantment tree view and the book library view.</p>
 *
 * <p>Also coordinates camera movement, tool slot updates, tooltip management,
 * and canvas-based node rendering for the enchanting system.</p>
 *
 * <p>This screen acts as the central controller for all client-side enchanting UI logic.</p>
 */
public class EnchantingTableScreen extends AbstractContainerScreen<@NotNull EnchantingTableMenu> {

    private static final Logger log = LogManager.getLogger(EnchantingTableScreen.class);
    private final Canvas scrollableCanvas;
    private final EnchantingTab enchantingTab;
    private final BookTab bookTab;
    private final TabButton tabButton;
    private final RegistryAccess registryAccess;
    private final CostRenderer enchantmentCostRenderer;
    private final TooltipManager tooltipManager;
    private final Player player;
    private CanvasCamera camera;
    private ScreenState screenState;
    private ItemStack lastToolSlotStack = ItemStack.EMPTY;
    private boolean isTabKeyDown;

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

    /**
     * Sets the current screen state (e.g. enchanting or books view).
     *
     * @param state new screen state
     */
    public void setState(ScreenState state) {
        this.screenState = state;
    }

    /**
     * Handles updates to the tool slot item.
     *
     * <p>Rebuilds enchantment branches, updates canvas layout, plays UI feedback,
     * and adjusts camera behavior based on the new item state.</p>
     *
     * @param newStack the new item in the tool slot
     */
    private void onToolSlotUpdate(ItemStack newStack) {
        enchantingTab.setDisplay(EnchantingTab.Display.ENCHANTMENTS);
        rebuildBranches(newStack);
        FxHelper.playToolSlotChanged(player().level());

        if (camera() == null) return;
        if (newStack.getItem().equals(lastToolSlotStack.getItem())) return;
        camera().setDraggingEnabled(!newStack.isEmpty());
        resetCamera();
    }

    private void rebuildBranches(ItemStack newStack) {
        enchantingTab.branchManager().buildBranches(newStack);
        canvas().setSizeToFitNodes(CostRegistry.client().getHighestLevel());
        enchantingTab.branchManager().positionBranches();
    }

    /**
     * @return the local player
     */
    public Player player() {
        return player;
    }

    /**
     * @return the UI camera controller
     */
    public CanvasCamera camera() {
        return camera;
    }

    /**
     * Reset camera zoom and recenter
     */
    private void resetCamera() {
        if (camera() == null) return;
        camera().setZoom(1f);
        camera().centerCameraOnCanvas();
    }

    /**
     * @return the main canvas used for node rendering
     */
    public Canvas canvas() {
        return scrollableCanvas;
    }

    /**
     * Initializes the screen camera and centers it on the canvas.
     */
    @Override
    protected void init() {
        super.init();
        this.camera = new CanvasCamera(this, getGuiLeft() + 4, getGuiTop() + 4);
        camera.centerCameraOnCanvas();
    }

    /**
     * Handles dynamic rendering state including tooltips, carried items,
     * and snapback animations.
     *
     * <p>Also manages tooltip lifecycle updates and clears invalid or stale tooltips.</p>
     */
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        List<Node> renderedNodes = enchantingTab.branchManager().getAllNodes();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 400);
        if (tooltipManager.hasActiveTooltip() && !renderedNodes.contains(tooltipManager.getActiveTooltipNode()))
            tooltipManager.clearActiveTooltip();

        if (tooltipManager.hasActiveTooltip())
            tooltipManager.getActiveTooltip().render(graphics, mouseX, mouseY);

        if (!tooltipManager.isTooltipLocked()) this.renderTooltip(graphics, mouseX, mouseY);

        if (!tooltipManager.isTooltipRequestedThisFrame() && !tooltipManager.isTooltipLocked())
            tooltipManager.clearActiveTooltip();

        tooltipManager.resetFrameState();
        graphics.pose().popPose();
    }

    /**
     * Renders inventory labels and adjusts label positioning.
     */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        this.inventoryLabelX = 16;
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
    }

    /**
     * Renders the main background layer including the canvas, active tab content,
     * and GUI texture.
     *
     * <p>Applies scissoring for canvas clipping and handles camera transforms
     * during rendering.</p>
     */
    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (!canvas().DEBUG_DISABLE_CULLING) graphics.enableScissor(
                camera.VIEWPORT_X, camera.VIEWPORT_Y,
                camera.VIEWPORT_X + camera.VIEWPORT_WIDTH, camera.VIEWPORT_Y + camera.VIEWPORT_HEIGHT);

        graphics.pose().pushPose();
        graphics.pose().translate(camera.VIEWPORT_X, camera.VIEWPORT_Y, 1);
        graphics.pose().scale(camera.zoom(), camera.zoom(), 1);
        graphics.pose().translate(-camera.x(), -camera.y(), 1);

        canvas().render(graphics);

        if (isState(ScreenState.ENCHANTING)) {
            enchantingTab.render(graphics, mouseX, mouseY);
        }

        graphics.pose().popPose();

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 2);
        if (isState(ScreenState.BOOKS)) {
            bookTab.render(graphics, mouseX, mouseY);
        }
        graphics.pose().popPose();

        if (!canvas().DEBUG_DISABLE_CULLING) graphics.disableScissor();

        RenderSystem.enableBlend();
        graphics.blit(
                Sprite.ENCHANTING_TABLE_GUI.id(),
                getGuiLeft(), getGuiTop(),
                0f, 0f,
                imageWidth, imageHeight,
                Sprite.ENCHANTING_TABLE_GUI.width(), Sprite.ENCHANTING_TABLE_GUI.height());
        RenderSystem.disableBlend();

        tabButton.render(graphics, mouseX, mouseY);

    }

    /**
     * Handles mouse click input for all UI components depending on active screen state.
     *
     * <p>Delegates clicks to tabs, camera, tool slots, and interactive widgets.</p>
     *
     * @return true if the event was consumed
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tabButton.onMouseClick(mouseX, mouseY, button)) return true;

        if (isState(ScreenState.ENCHANTING)) {
            if (enchantingTab.centralSlot().onMouseClick(mouseX, mouseY, button)) return true;

            if (tooltipManager.hasActiveTooltip() && tooltipManager.getActiveTooltip().onMouseClick(mouseX, mouseY, button))
                return true;

            if (camera.onMouseClick(mouseX, mouseY, button)) return true;
        } else if (isState(ScreenState.BOOKS)) {
            if (bookTab.scrollbar().onMouseClick(mouseX, mouseY, button)) return true;

            for (FilterCheckbox checkbox : bookTab.filterCheckboxes())
                if (checkbox.onMouseClick(mouseX, mouseY, button)) return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Handles mouse drag input for camera movement and scrollable UI elements.
     *
     * @return true if the event was consumed
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (camera.onMouseDrag(mouseX, mouseY, button, dragX / camera().zoom(), dragY / camera().zoom())) return true;

        if (bookTab.scrollbar().onMouseDrag(mouseX, mouseY, button, dragX, dragY)) return true;

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    /**
     * Handles mouse release events for camera, tooltips, and UI components.
     *
     * @return true if the event was consumed
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (tooltipManager().hasActiveTooltip() && tooltipManager().getActiveTooltip().onMouseRelease(mouseX, mouseY, button))
            return true;

        if (camera.onMouseRelease(mouseX, mouseY, button)) return true;

        if (bookTab.scrollbar().onMouseRelease(mouseX, mouseY, button)) return true;

        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Handles keyboard input for the screen.
     *
     * <p>In book mode, supports search input and backspace handling.</p>
     *
     * @return true if the event was consumed
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isState(ScreenState.BOOKS)) {
            if (keyCode == InputConstants.KEY_BACKSPACE) {
                bookTab.searchbar().removeCharFromSearch();
            }

            if (keyCode == InputConstants.KEY_ESCAPE) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            } else {
                return true;
            }
        }

        if (isState(ScreenState.ENCHANTING)) {
            if (keyCode == InputConstants.KEY_TAB) {
                isTabKeyDown = true;
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Periodically checks for changes in the tool slot and triggers updates
     * when the item changes.
     */
    @Override
    protected void containerTick() {
        ItemStack stack = getMenu().getToolSlot().getItem();

        if (!ItemStack.isSameItemSameComponents(stack, lastToolSlotStack)) {
            onToolSlotUpdate(stack);
            lastToolSlotStack = stack.copy();
        }

        if (isTabKeyDown) {
            if (enchantingTab().isDisplay(EnchantingTab.Display.ENCHANTMENTS)
                    && getMenu().getToolSlot().hasItem()
                    && !getMenu().getToolSlot().getItem().is(ModItems.ANCIENT_BOOK.get())) {
                enchantingTab.setDisplay(EnchantingTab.Display.MOD_FILTERS);
                FxHelper.playTabDown(player());
                rebuildBranches();
                resetCamera();
                tooltipManager().unlockTooltip();
            }
        } else {
            if (enchantingTab().isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
                enchantingTab.setDisplay(EnchantingTab.Display.ENCHANTMENTS);
                FxHelper.playTabUp(player());
                rebuildBranches();
                resetCamera();
            }
        }


    }

    /**
     * @return tooltip manager for interactive UI elements
     */
    public TooltipManager tooltipManager() {
        return tooltipManager;
    }

    /**
     * Checks whether the screen is currently in the given state.
     *
     * @param state state to check
     * @return true if active
     */
    public boolean isState(ScreenState state) {
        return this.screenState == state;
    }

    /**
     * Handles scroll input for either camera zoom or book tab scrolling,
     * depending on the active screen state.
     *
     * @return true if the event was consumed
     */
    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (isState(ScreenState.ENCHANTING)) {
            if (camera.onMouseScrolled(x, y, scrollY)) return true;
        } else if (isState(ScreenState.BOOKS)) {
            if (bookTab.scrollbar().onMouseScrolled(x, y, scrollY)) return true;
        }

        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_TAB) {
            isTabKeyDown = false;
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    /**
     * Handles character input for text entry in book search mode.
     *
     * @return true if the event was consumed
     */
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (isState(ScreenState.BOOKS)) {
            bookTab.searchbar().addCharToSearch(codePoint);
        }

        return super.charTyped(codePoint, modifiers);
    }

    /**
     * Checks whether the mouse is inside a given rectangular area.
     *
     * @param x      top-left x position
     * @param y      top-left y position
     * @param width  rectangle width
     * @param height rectangle height
     * @param mouseX current mouse x
     * @param mouseY current mouse y
     * @return true if the mouse is within bounds
     */
    public boolean isMouseOver(double x, double y, int width, int height, double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    public void rebuildBranches() {
        rebuildBranches(lastToolSlotStack);
    }

    /**
     * @return registry access for game data lookups
     */
    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    /**
     * @return renderer for enchantment costs
     */
    public CostRenderer enchantmentCostRenderer() {
        return enchantmentCostRenderer;
    }

    /**
     * @return book tab UI controller
     */
    public BookTab bookTab() {
        return bookTab;
    }

    public EnchantingTab enchantingTab() {
        return enchantingTab;
    }
}
