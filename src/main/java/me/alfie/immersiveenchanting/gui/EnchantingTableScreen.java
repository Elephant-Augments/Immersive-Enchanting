package me.alfie.immersiveenchanting.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.alfinolib.gui.CommonAbstractContainerScreen;
import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.client.ModKeyMappings;
import me.alfie.immersiveenchanting.datapack.enchantment_cost.CostRegistry;
import me.alfie.immersiveenchanting.api.node.internal.ModFilterNodeData;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasCamera;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.tab.TabButton;
import me.alfie.immersiveenchanting.gui.tab.book.BookTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.CostRenderer;
import me.alfie.immersiveenchanting.gui.tab.enchanting.tooltip.TooltipManager;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.tags.ModEnchantmentTags;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
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
public class EnchantingTableScreen extends CommonAbstractContainerScreen<@NotNull EnchantingTableMenu> {
    private CanvasCamera camera;
    private final Canvas scrollableCanvas;
    private ScreenState screenState;

    private final EnchantingTab enchantingTab;
    private final BookTab bookTab;
    private final TabButton tabButton;
    private final ModFilterHelpHint modFilterHelpHint;

    private ItemStack lastToolSlotStack = ItemStack.EMPTY;

    private final Player player;
    private final RegistryAccess registryAccess;

    private final CostRenderer enchantmentCostRenderer;
    private final TooltipManager tooltipManager;

    private static @Nullable String rememberedFilteredModid = "minecraft";
    private static boolean rememberedUnlockedOnly;

    private @Nullable String filteredModid = rememberedFilteredModid;
    private boolean unlockedOnly = rememberedUnlockedOnly;
    private boolean isTabKeyDown;
    private ItemStack deferredCentralItemTooltip = ItemStack.EMPTY;
    @Nullable
    private Component deferredTabTooltip;

    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, EnchantingTableLayout.GUI_WIDTH, EnchantingTableLayout.GUI_HEIGHT);
        registryAccess = inventory.player.registryAccess();
        this.player = inventory.player;
        this.scrollableCanvas = new Canvas(this);
        setState(ScreenState.ENCHANTING);

        enchantingTab = new EnchantingTab(this);
        bookTab = new BookTab(this);
        tabButton = new TabButton(this);
        modFilterHelpHint = new ModFilterHelpHint(this);

        this.enchantmentCostRenderer = new CostRenderer(CostRegistry.client());
        this.tooltipManager = new TooltipManager(this);

        if (isShowingCosmeticsOnly()
                && registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                        .listElements()
                        .noneMatch(ModEnchantmentTags::isCosmetic)) {
            this.filteredModid = "minecraft";
            this.unlockedOnly = false;
            rememberCurrentFilter();
        }

        onToolSlotUpdate(ItemStack.EMPTY);
    }

    /**
     * Initializes the screen camera and centers it on the canvas.
     */
    @Override
    protected void init() {
        super.init();
        this.leftPos = EnchantingTableLayout.guiLeft(this.width, EnchantingTableLayout.isJeiLoaded());
        this.topPos = EnchantingTableLayout.guiTop(this.height);
        EnchantingTableLayout.CanvasViewport viewport = EnchantingTableLayout.canvasViewport(getGuiLeft(), getGuiTop());
        this.camera = new CanvasCamera(this, viewport.x(), viewport.y(), viewport.width(), viewport.height());
        camera.centerCameraOnCanvas();
    }

    @Override
    public void renderBackground(GuiGraphicsX gx, MousePos mousePos, float partialTick) {
        super.renderBackground(gx, mousePos, partialTick);

        if(!canvas().DEBUG_DISABLE_CULLING) gx.graphics().enableScissor(
                camera.VIEWPORT_X, camera.VIEWPORT_Y,
                camera.VIEWPORT_X + camera.VIEWPORT_WIDTH, camera.VIEWPORT_Y + camera.VIEWPORT_HEIGHT);

        gx.graphics().pose().pushPose();
        gx.graphics().pose().translate(camera.VIEWPORT_X, camera.VIEWPORT_Y, 1);
        gx.graphics().pose().scale(camera.zoom(), camera().zoom(), 1);
        gx.graphics().pose().translate(-camera.x(), -camera.y(), 1);

        canvas().render(gx);

        boolean showModPicker = enchantingTab.isDisplay(EnchantingTab.Display.MOD_FILTERS);
        if(isState(ScreenState.ENCHANTING) || showModPicker) enchantingTab.render(gx, mousePos);
        gx.graphics().pose().popPose();

        gx.graphics().pose().pushPose();
        gx.graphics().pose().translate(0, 0, 2);
        if(isState(ScreenState.BOOKS) && !showModPicker) bookTab.render(gx, mousePos);
        gx.graphics().pose().popPose();

        if(!canvas().DEBUG_DISABLE_CULLING) gx.graphics().disableScissor();

        RenderSystem.enableBlend();
        drawGUI(gx.graphics(), getGuiLeft(), getGuiTop());
        RenderSystem.disableBlend();


        tabButton.render(gx, mousePos);
    }

    @Override
    public void render(GuiGraphicsX gx, MousePos mousePos, float partialTick) {
        super.render(gx, mousePos, partialTick);
        
        //Update tooltip manager
        List<Node> renderedNodes = enchantingTab.branchManager().getAllNodes();

        gx.graphics().pose().pushPose();
        gx.graphics().pose().translate(0, 0, 400);
        modFilterHelpHint.render(gx, mousePos);
        if(tooltipManager.hasActiveTooltip() && !renderedNodes.contains(tooltipManager.getActiveTooltipNode()))
            tooltipManager.clearActiveTooltip();

        // Workaround to call custom overlays first, as AlfinoLib's render(GuiGraphics) is final and this screen never
        // reaches a vanilla render() override that would call renderTooltip itself.
        if(tooltipManager.hasActiveTooltip()) {
            tooltipManager.getActiveTooltip().render(gx, mousePos);
        } else if(!deferredCentralItemTooltip.isEmpty()) {
            gx.graphics().renderTooltip(this.font, deferredCentralItemTooltip, mousePos.x(), mousePos.y());
        } else if(deferredTabTooltip != null) {
            gx.graphics().renderTooltip(this.font, deferredTabTooltip, mousePos.x(), mousePos.y());
        } else if(!tooltipManager.isTooltipLocked()) {
            this.renderTooltip(gx.graphics(), mousePos.x(), mousePos.y());
        }

        if(!tooltipManager.isTooltipRequestedThisFrame() && !tooltipManager.isTooltipLocked())
            tooltipManager.clearActiveTooltip();

        tooltipManager.resetFrameState();
        deferredCentralItemTooltip = ItemStack.EMPTY;
        deferredTabTooltip = null;
        gx.graphics().pose().popPose();
    }

    /**
     * Suppresses vanilla menu-slot tooltips if AbstractContainerScreen also invokes this
     * during {@code super.render}, so an overlay isn't drawn underneath.
     */
    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if(tooltipManager.hasActiveTooltip()
                || !deferredCentralItemTooltip.isEmpty()
                || deferredTabTooltip != null
                || tooltipManager.isTooltipLocked()) {
            return;
        }
        super.renderTooltip(guiGraphics, x, y);
    }

    /**
     * Requests that the tool-slot item tooltip be drawn once at end-of-frame in screen space.
     */
    public void requestCentralItemTooltip(ItemStack stack) {
        this.deferredCentralItemTooltip = stack;
    }

    /**
     * Requests that the tab-button label tooltip be drawn once at end-of-frame in screen space.
     */
    public void requestTabTooltip(Component tooltip) {
        this.deferredTabTooltip = tooltip;
    }

    /**
     * @return {@code true} if the picker is showing every enchantment.
     */
    public boolean isShowingAll() {
        return !unlockedOnly && filteredModid == null;
    }

    /**
     * @return {@code true} if the picker is showing only unlocked enchantments.
     */
    public boolean isShowingUnlockedOnly() {
        return unlockedOnly;
    }

    /**
     * @return {@code true} if the picker is showing cosmetic enchantments.
     */
    public boolean isShowingCosmeticsOnly() {
        return !unlockedOnly && ModFilterNodeData.COSMETICS.equals(filteredModid);
    }

    /**
     * @return {@code true} if this enchantment should appear under the current filter.
     */
    public boolean matchesCurrentFilter(Holder<Enchantment> enchantment) {
        boolean cosmetic = ModEnchantmentTags.isCosmetic(enchantment);
        if (unlockedOnly) {
            return !cosmetic && getMenu().isEnchantmentAvailable(enchantment);
        }
        if (isShowingCosmeticsOnly()) {
            return cosmetic;
        }
        if (cosmetic) {
            return false;
        }
        String namespace = enchantment.unwrapKey()
                .map(key -> key.location().getNamespace())
                .orElse(null);
        return filteredModid == null || filteredModid.equals(namespace);
    }

    /**
     * @return the selected mod namespace, or {@code null} when showing all.
     */
    public @Nullable String filteredModid() {
        return filteredModid;
    }

    /**
     * Applies a selection from the hold-to-filter picker.
     */
    public void selectModFromPicker(@Nullable String modid) {
        this.unlockedOnly = false;
        this.filteredModid = modid;
        rememberCurrentFilter();
        bookTab.scrollbar().resetScrollIndex();
        rebuildBranches();
    }

    public void selectUnlockedFromPicker() {
        this.unlockedOnly = true;
        rememberCurrentFilter();
        bookTab.scrollbar().resetScrollIndex();
        rebuildBranches();
    }

    private void rememberCurrentFilter() {
        rememberedFilteredModid = filteredModid;
        rememberedUnlockedOnly = unlockedOnly;
    }

    @Override
    public void renderLabels(GuiGraphicsX gx, MousePos mousePos) {
        GuiGraphicsApi.text(
                gx,
                this.font,
                this.playerInventoryTitle.copy().withColor(-12566464),
                EnchantingTableLayout.inventoryLabelX(menu),
                EnchantingTableLayout.inventoryLabelY(menu),
                false
        );
    }

    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(tabButton.onMouseClick(mousePos, button)) return true;

        if(isState(ScreenState.ENCHANTING) || enchantingTab.isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            if(enchantingTab.centralSlot().onMouseClick(mousePos, button)) return true;

            if(tooltipManager.hasActiveTooltip() && tooltipManager.getActiveTooltip().onMouseClick(mousePos, button)) return true;

            if(camera.onMouseClick(mousePos, button)) return true;
        } else if(isState(ScreenState.BOOKS)) {
            if(bookTab.scrollbar().onMouseClick(mousePos, button)) return true;
        }

        return super.onMouseClick(mousePos, button);
    }

    @Override
    public boolean onMouseDrag(MousePos mousePos, int button, double dx, double dy) {
        if(camera.onMouseDrag(mousePos, button, dx / camera().zoom(), dy / camera().zoom())) return true;

        if(bookTab.scrollbar().onMouseDrag(mousePos, button, dx, dy)) return true;

        return super.onMouseDrag(mousePos, button, dx, dy);
    }

    @Override
    public boolean onMouseRelease(MousePos mousePos, int button) {
        if(tooltipManager().hasActiveTooltip() && tooltipManager().getActiveTooltip().onMouseRelease(mousePos, button)) return true;

        if(camera.onMouseRelease(mousePos, button)) return true;

        if(bookTab.scrollbar().onMouseRelease(mousePos, button)) return true;

        return super.onMouseRelease(mousePos, button);
    }

    @Override
    public boolean onMouseScrolled(MousePos mousePos, double scrollY) {
        if(isState(ScreenState.ENCHANTING) || enchantingTab.isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            if(camera.onMouseScrolled(mousePos, scrollY)) return true;
        } else if(isState(ScreenState.BOOKS)) {
            if(bookTab.scrollbar().onMouseScrolled(mousePos, scrollY)) return true;
        }

        return super.onMouseScrolled(mousePos, scrollY);
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if(ModKeyMappings.isModFilterKey(keyCode, scanCode)) {
            isTabKeyDown = true;
            return true;
        }

        if(isState(ScreenState.BOOKS)) {
            if(keyCode == InputConstants.KEY_BACKSPACE) {
                bookTab.searchbar().removeCharFromSearch();
            }

            if(keyCode == InputConstants.KEY_ESCAPE) {
                return super.onKeyPress(keyCode, scanCode, modifiers);
            } else {
                return true;
            }
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if(ModKeyMappings.isModFilterKey(keyCode, scanCode)) {
            isTabKeyDown = false;
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean onCharTyped(char codePoint, int modifiers) {
        if(isState(ScreenState.BOOKS)) {
            bookTab.searchbar().addCharToSearch(codePoint);
        }

        return super.onCharTyped(codePoint, modifiers);
    }

    /**
     * Periodically checks for changes in the tool slot and triggers updates
     * when the item changes.
     */
    @Override
    protected void containerTick() {
        ItemStack stack = getMenu().getToolSlot().getItem();

        if(!ItemStack.isSameItemSameComponents(stack, lastToolSlotStack)) {
            onToolSlotUpdate(stack);
            lastToolSlotStack = stack.copy();
        }

        updateModPickerDisplay();
    }

    private boolean canShowModPicker() {
        if(isState(ScreenState.BOOKS)) return true;
        return getMenu().getToolSlot().hasItem()
                && !getMenu().getToolSlot().getItem().is(ModItems.ANCIENT_BOOK.get());
    }

    private void updateModPickerDisplay() {
        if(isTabKeyDown && canShowModPicker()) {
            if(enchantingTab.isDisplay(EnchantingTab.Display.ENCHANTMENTS)) {
                enchantingTab.setDisplay(EnchantingTab.Display.MOD_FILTERS);
                FxHelper.playTabDown(player());
                rebuildBranches();
                resetCamera();
                tooltipManager().unlockTooltip();
            }
        } else if(enchantingTab.isDisplay(EnchantingTab.Display.MOD_FILTERS)) {
            enchantingTab.setDisplay(EnchantingTab.Display.ENCHANTMENTS);
            FxHelper.playTabUp(player());
            rebuildBranches();
            resetCamera();
        }
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
        if (!menu.getToolSlot().getItem().isEmpty()) FxHelper.playToolSlotChanged(player());

        if(camera() == null) return;
        if(newStack.getItem().equals(lastToolSlotStack.getItem())) return;
        camera().setDraggingEnabled(!newStack.isEmpty());
        resetCamera();
    }

    public void rebuildBranches() {
        rebuildBranches(lastToolSlotStack);
    }

    private void rebuildBranches(ItemStack newStack) {
        enchantingTab.branchManager().buildBranches(newStack);
        canvas().setSizeToFitNodes(CostRegistry.client().getHighestLevel());
        enchantingTab.branchManager().positionBranches();
    }

    /**
     * Reset camera zoom and recenter
     */
    private void resetCamera() {
        if(camera() == null) return;
        camera().setZoom(1f);
        camera().centerCameraOnCanvas();
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
     * Checks whether the screen is currently in the given state.
     *
     * @param state state to check
     * @return true if active
     */
    public boolean isState(ScreenState state) {
        return this.screenState == state;
    }

    /** @return the main canvas used for node rendering */
    public Canvas canvas() {
        return scrollableCanvas;
    }

    /** @return the UI camera controller */
    public CanvasCamera camera() {
        return camera;
    }

    /** @return registry access for game data lookups */
    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    /** @return the local player */
    public Player player() {
        return player;
    }

    /** @return renderer for enchantment costs */
    public CostRenderer enchantmentCostRenderer() {
        return enchantmentCostRenderer;
    }

    /** @return tooltip manager for interactive UI elements */
    public TooltipManager tooltipManager() {
        return tooltipManager;
    }

    /** @return book tab UI controller */
    public BookTab bookTab() {
        return bookTab;
    }

    /** @return enchanting tab UI controller */
    public EnchantingTab enchantingTab() {
        return enchantingTab;
    }

    /** @return the screen font */
    public Font getFont() {
        return font;
    }

    /**
     * Composites the full GUI from the original textures.
     */
    private static void drawGUI(GuiGraphics graphics, int guiLeft, int guiTop) {
        ResourceLocation texture = Sprite.ENCHANTING_TABLE_GUI.id().mc();
        drawViewportFrame(graphics, texture, guiLeft, guiTop);
        drawInventory(graphics, texture, guiLeft, guiTop);
        drawTabs(graphics, texture, guiLeft, guiTop);
        drawCostPanel(graphics, texture, guiLeft, guiTop);
    }

    /**
     * Draws the enchanting viewport and its chrome border.
     */
    private static void drawViewportFrame(
            GuiGraphics graphics, ResourceLocation texture, int guiLeft, int guiTop
    ) {
        int x = guiLeft;
        int y = guiTop;
        int w = EnchantingTableLayout.GUI_WIDTH;
        int h = EnchantingTableLayout.VIEWPORT_BOTTOM_IN_GUI;
        int tex = EnchantingTableLayout.TEXTURE_SIZE;
        int holeBottom = EnchantingTableLayout.TEXTURE_VIEWPORT_BOTTOM;
        int border = EnchantingTableLayout.CANVAS_INSET;
        int bottomBorder = EnchantingTableLayout.VIEWPORT_BOTTOM_BORDER;
        int innerW = w - border * 2;
        int innerH = h - border - bottomBorder;
        int innerUW = tex - border * 2;
        int innerVH = holeBottom - border - bottomBorder;
        int fillU = EnchantingTableLayout.TEXTURE_TAB_FILL_U;
        int fillV = EnchantingTableLayout.TEXTURE_TAB_FILL_V;
        int blackU = EnchantingTableLayout.TEXTURE_FRAME_BLACK_U;
        int blackV = EnchantingTableLayout.TEXTURE_FRAME_BLACK_V;

        blitRegion(graphics, texture, x, y, border, border, 0, 0, border, border, tex, tex);
        blitRegion(graphics, texture, x + w - border, y, border, border, tex - border, 0, border, border, tex, tex);
        blitRegionVFlip(graphics, texture, x, y + h - bottomBorder, border, bottomBorder, 0, 0, border, bottomBorder, tex, tex);
        blitRegionVFlip(graphics, texture, x + w - border, y + h - bottomBorder, border, bottomBorder, tex - border, 0, border, bottomBorder, tex, tex);

        blitRegion(graphics, texture, x + border, y, innerW, border, border, 0, innerUW, border, tex, tex);
        blitRegion(
                graphics, texture,
                x + 3, y + h - bottomBorder, w - 6, bottomBorder - 1,
                fillU, fillV, 1, 1, tex, tex
        );
        blitRegion(
                graphics, texture,
                x + border, y + h - 1, innerW, 1,
                blackU, blackV, 1, 1, tex, tex
        );

        blitRegion(graphics, texture, x, y + border, border, innerH, 0, border, border, innerVH, tex, tex);
        blitRegion(graphics, texture, x + w - border, y + border, border, innerH, tex - border, border, border, innerVH, tex, tex);

        drawViewport(graphics, texture, x + border, y + border, innerW, innerH, border, innerUW, innerVH, tex);
    }

    /**
     * Draws only the inner hole of the viewport, where the enchanting screen is rendered.
     */
    private static void drawViewport(
            GuiGraphics graphics, ResourceLocation texture,
            int x, int y, int w, int h,
            int border, int innerUW, int innerVH, int tex
    ) {
        blitRegion(graphics, texture, x, y, w, h, border, border, innerUW, innerVH, tex, tex);
    }

    /**
     * Draws the bottom inventory panel under the viewport.
     */
    private static void drawInventory(
            GuiGraphics graphics, ResourceLocation texture, int guiLeft, int guiTop
    ) {
        int tex = EnchantingTableLayout.TEXTURE_SIZE;
        int bottomY = guiTop + EnchantingTableLayout.VIEWPORT_BOTTOM_IN_GUI;
        int chromeV = EnchantingTableLayout.TEXTURE_VIEWPORT_FRAME_BOTTOM;
        int chromeH = EnchantingTableLayout.BOTTOM_CHROME_HEIGHT;
        blitRegion(
                graphics, texture,
                guiLeft + EnchantingTableLayout.INVENTORY_PANEL_X, bottomY,
                EnchantingTableLayout.TEXTURE_INVENTORY_WIDTH, chromeH,
                EnchantingTableLayout.TEXTURE_INVENTORY_U, chromeV,
                EnchantingTableLayout.TEXTURE_INVENTORY_WIDTH, chromeH,
                tex, tex
        );
    }

    /**
     * Draws the book/enchanting tab.
     */
    private static void drawTabs(
            GuiGraphics graphics, ResourceLocation texture, int guiLeft, int guiTop
    ) {
        int tex = EnchantingTableLayout.TEXTURE_SIZE;
        blitTabSprite(graphics, texture, guiLeft, guiTop, tex);
    }

    private static void blitTabSprite(
            GuiGraphics graphics, ResourceLocation texture,
            int guiLeft, int guiTop, int tex
    ) {
        blitRegion(
                graphics, texture,
                guiLeft + EnchantingTableLayout.TAB_SPRITE_X,
                guiTop + EnchantingTableLayout.TAB_SPRITE_Y,
                EnchantingTableLayout.TEXTURE_TAB_WIDTH, EnchantingTableLayout.TEXTURE_TAB_HEIGHT,
                EnchantingTableLayout.TEXTURE_TAB_U, EnchantingTableLayout.TEXTURE_TAB_V,
                EnchantingTableLayout.TEXTURE_TAB_WIDTH, EnchantingTableLayout.TEXTURE_TAB_HEIGHT,
                tex, tex
        );
    }

    /**
     * Draws the right-side cost/gear panel under the viewport.
     */
    private static void drawCostPanel(
            GuiGraphics graphics, ResourceLocation texture, int guiLeft, int guiTop
    ) {
        int tex = EnchantingTableLayout.TEXTURE_SIZE;
        int bottomY = guiTop + EnchantingTableLayout.VIEWPORT_BOTTOM_IN_GUI;
        int chromeV = EnchantingTableLayout.TEXTURE_VIEWPORT_FRAME_BOTTOM;
        int chromeH = EnchantingTableLayout.BOTTOM_CHROME_HEIGHT;
        blitRegion(
                graphics, texture,
                guiLeft + EnchantingTableLayout.COST_PANEL_X, bottomY,
                EnchantingTableLayout.TEXTURE_COST_WIDTH, chromeH,
                EnchantingTableLayout.TEXTURE_COST_U, chromeV,
                EnchantingTableLayout.TEXTURE_COST_WIDTH, chromeH,
                tex, tex
        );
    }

    private static void blitRegionVFlip(
            GuiGraphics graphics, ResourceLocation texture,
            int x, int y, int w, int h,
            int u, int v, int uW, int vH,
            int texW, int texH
    ) {
        blitRegion(graphics, texture, x, y, w, h, u, v + vH, uW, -vH, texW, texH);
    }

    private static void blitRegion(
            GuiGraphics graphics, ResourceLocation texture,
            int x, int y, int w, int h,
            int u, int v, int uW, int vH,
            int texW, int texH
    ) {
        graphics.blit(texture, x, y, w, h, (float) u, (float) v, uW, vH, texW, texH);
    }
}
