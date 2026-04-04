package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.gui.tab.enchanting.EnchantingTab;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class EnchantingTableScreen extends AbstractContainerScreen<EnchantingTableMenu> {


    private final ScrollableCanvas scrollableCanvas;
    private ScreenState screenState;

    private final EnchantingTab enchantingTab;

    private ItemStack lastToolSlotStack = ItemStack.EMPTY;

    private final RegistryAccess registryAccess;

    public EnchantingTableScreen(EnchantingTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 256, 222);
        registryAccess = inventory.player.registryAccess();

        this.scrollableCanvas = new ScrollableCanvas(this);
        setState(ScreenState.ENCHANTING);

        enchantingTab = new EnchantingTab(this);

        onToolSlotUpdate(ItemStack.EMPTY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        canvas().setScale(2f);

        graphics.pose().pushMatrix();
        graphics.pose().scale(canvas().scale());
        canvas().render(graphics);
        enchantingTab.render(graphics, mouseX, mouseY);
        graphics.pose().popMatrix();

        graphics.blit(RenderPipelines.GUI_TEXTURED,
                Sprite.ENCHANTING_TABLE_GUI.get(),
                getGuiLeft(), getGuiTop(),
                0f,0f,
                imageWidth, imageHeight,
                Sprite.ENCHANTING_TABLE_GUI.width(), Sprite.ENCHANTING_TABLE_GUI.height());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouse, boolean doubleClick) {
        if(isState(ScreenState.ENCHANTING)) {
            if(enchantingTab.centralSlot().onMouseClick(mouse)) return true;

            if(canvas().onMouseClick(mouse)) return true;
        }

        return super.mouseClicked(mouse, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouse, double dx, double dy) {
        if(canvas().onMouseDrag(mouse, dx, dy)) return true;

        return super.mouseDragged(mouse, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouse) {
        if(canvas().onMouseRelease(mouse)) return true;

        return super.mouseReleased(mouse);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
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
        canvas().setDragLocked(newStack.isEmpty());

        if(isState(ScreenState.ENCHANTING)) enchantingTab.buildBranches(newStack);

        canvas().resizeAndCenter(16);
    }

    public void setState(ScreenState state) {
        this.screenState = state;
    }

    public boolean isState(ScreenState state) {
        return this.screenState == state;
    }

    public ScrollableCanvas canvas() {
        return scrollableCanvas;
    }

    public RegistryAccess registryAccess() {
        return registryAccess;
    }
}
