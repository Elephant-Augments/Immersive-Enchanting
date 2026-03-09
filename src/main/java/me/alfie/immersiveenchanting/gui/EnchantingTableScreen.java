package me.alfie.immersiveenchanting.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.immersiveenchanting.config.ServerConfig;
import me.alfie.immersiveenchanting.gui.core.*;
import me.alfie.immersiveenchanting.gui.core.tab.book.BookTab;
import me.alfie.immersiveenchanting.gui.core.tab.book.FilterCheckbox;
import me.alfie.immersiveenchanting.gui.core.tab.TabButton;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.EnchantingTab;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.Node;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.enchanting.EnchantingNode;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.replicate.ReplicateNode;
import me.alfie.immersiveenchanting.gui.core.tab.enchanting.node.transmute.TransmuteNode;
import me.alfie.immersiveenchanting.networking.packet.enchantitem.EnchantItemPacket;
import me.alfie.immersiveenchanting.networking.packet.removeenchantment.RemoveEnchantmentPacket;
import me.alfie.immersiveenchanting.networking.packet.replicatebookpacket.ReplicateBookPacket;
import me.alfie.immersiveenchanting.networking.packet.transmutebookpacket.TransmuteBookPacket;
import me.alfie.immersiveenchanting.networking.packet.updatetoolslot.UpdateToolSlotPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class EnchantingTableScreen extends AbstractContainerScreen<EnchantingTableMenu> {
    public final Player player;
    ScrollableCanvas canvas;

    private long holdStartTime;
    public final long HOLD_THRESHOLD = 1000;

    private final TabButton tabButton;
    public final BookTab bookTab;
    public final EnchantingTab enchantingTab;
    private ScreenState screenState = ScreenState.ENCHANTING;


    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        canvas = new ScrollableCanvas(this);

        this.titleLabelX = 10;
        this.inventoryLabelX = 10;
        this.imageHeight = 222;
        this.imageWidth = 256;

        // Center the canvas inside the viewport at start
        canvas.setScrollX((canvas.getWidth() / 2.0) - (canvas.VIEWPORT_WIDTH / 2.0));
        canvas.setScrollY((canvas.getHeight() / 2.0) - (canvas.VIEWPORT_HEIGHT / 2.0));

        //No branch shenanigans here, do it in init() pls <3
        this.player = playerInventory.player;

        enchantingTab = new EnchantingTab(this);
        bookTab = new BookTab(this);
        tabButton = new TabButton(this);
    }

    public ScrollableCanvas getCanvas() {
        return this.canvas;
    }

    @Override //Init code when GUI is created.
    public void init() {
        super.init();

        enchantingTab.init();
        enchantingTab.onToolSlotUpdate(); //Updates if screen size is changed
    }


    /**
     * Returns true if mouse is over the bounds given. Note: this should only be used for static elements on the screen.
     * <br>For scrollable canvas elements, use ScrollableCanvas.isMouseOverBoundingBox
     * @param mouseX
     * @param mouseY
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    /**
     * Main render code.
     * @param guiGraphics
     * @param partialTick
     * @param mouseX
     * @param mouseY
     */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if(getState().equals(ScreenState.ENCHANTING)) {
            enchantingTab.render(guiGraphics, mouseX, mouseY);
        } else if(getState().equals(ScreenState.BOOKS)) {
            bookTab.render(guiGraphics);
        }

        //Disable scissor after drawing
        RenderSystem.disableScissor();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(Sprite.ENCHANTING_TABLE_BACKGROUND.get(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        RenderSystem.disableBlend();

        //Draw tab icon after scissor
        tabButton.render(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if(getState().equals(ScreenState.ENCHANTING)) {
            enchantingTab.renderTooltipItemStackCost(guiGraphics, mouseX, mouseY);
            enchantingTab.centralSlot.renderTooltip(guiGraphics, mouseX, mouseY);
        }


        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if(tabButton.onMouseClick((int) mouseX, (int) mouseY)) return true;

            if(getState().equals(ScreenState.BOOKS)) {
                for(FilterCheckbox filterCheckbox : bookTab.filterCheckboxes) {
                    if(filterCheckbox.onMouseClick((int) mouseX, (int) mouseY)) return true;
                }

                if(bookTab.scrollbar.onMouseClick((int) mouseX, (int) mouseY)) return true;
            } else if(getState().equals(ScreenState.ENCHANTING)) {
                //Node clicked
                if (enchantingTab.getHoveredNode() != null) {
                    if (enchantingTab.getHoveredNode().isMouseOver(canvas, mouseX, mouseY)) {
                        if(enchantingTab.getHoveredNode().onClicked((int) mouseX, (int) mouseY)) return true;
                    }
                }

                if(enchantingTab.centralSlot.onMouseClick((int) mouseX, (int) mouseY)) return true;

                if(canvas.isDraggingEnabled() && canvas.startDrag(mouseX, mouseY)) return true;
            }
        }

        if (button == 1) if(enchantingTab.onRightClick((int) mouseX, (int) mouseY)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(getState().equals(ScreenState.ENCHANTING)) {
            if(canvas.drag(mouseX, mouseY, button)) return true;
        } else if(getState().equals(ScreenState.BOOKS)) {
            if(bookTab.scrollbar.updateScrollFromMouse((int) mouseY)) return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        enchantingTab.setHeldNode(null);
        bookTab.scrollbar.isMouseDraggingScroller = false;
        holdStartTime = 0;
        canvas.stopDrag(button);

        return super.mouseReleased(mouseX, mouseY, button);

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        bookTab.scrollbar.incrementScrollIndex((int) -scrollY);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void mouseHeld() {
        enchantingTab.onNodeHeld();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(getState().equals(ScreenState.BOOKS)) {
            if(keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                bookTab.searchbar.removeCharFromSearch();
            }

            if(keyCode == GLFW.GLFW_KEY_ESCAPE) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            } else {
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if(getState().equals(ScreenState.BOOKS)) {
            bookTab.searchbar.addCharToSearch(codePoint);
        }

        return super.charTyped(codePoint, modifiers);
    }

    /**
     * Detect if item changes in tool slot.
     */
    @Override
    public void containerTick() {
        super.containerTick();
        enchantingTab.checkToolSlotUpdated();
    }


    public void setHoldStartTime(long time) {
        this.holdStartTime = time;
    }

    public long getHoldStartTime() {
        return this.holdStartTime;
    }

    public void setState(ScreenState state) {
        this.screenState = state;

        if(this.screenState.equals(ScreenState.BOOKS)) {
            canvas.setDraggingEnabled(false);
            bookTab.searchbar.clearSearch();
            bookTab.scrollbar.resetScrollIndex();
            bookTab.resetFilterBoxes();
        } else if (this.screenState.equals(ScreenState.ENCHANTING)) {
            if(!getMenu().isToolSlotEmpty()) {
                canvas.setDraggingEnabled(true);
            }
        }
    }

    public ScreenState getState() {
        return screenState;
    }
}

