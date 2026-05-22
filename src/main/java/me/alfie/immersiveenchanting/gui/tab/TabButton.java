package me.alfie.immersiveenchanting.gui.tab;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class TabButton implements ScreenEventListener {

    public final EnchantingTableScreen screen;

    private final int width = 20;
    private final int height = 25;
    private final int x = 200;
    private final int y = 126;

    private ItemStack icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
    private Component label = Component.empty();

    /**
     * Creates a new tab button tied to the given screen.
     * <p>
     *
     * @param screen the parent {@link EnchantingTableScreen} this button belongs to
     */
    public TabButton(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    /**
     * Renders the tab button, including its icon and hover tooltip.
     * <P>
     * <p>The displayed icon and label depend on the current {@link ScreenState}:</p>
     * <p>
     * <ul>
     *     <li>ENCHANTING → shows book icon (switch to books view)</li>
     *     <li>BOOKS → shows enchanting table icon (switch to enchanting view)</li>
     * </ul>
     * <p>
     *
     * @param graphics the GUI graphics context used for rendering
     * @param mouseX   the current mouse X position
     * @param mouseY   the current mouse Y position
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        if (screen.isState(ScreenState.ENCHANTING)) {
            label = Component.translatable("immersiveenchanting.tab.book");
            icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
        } else if (screen.isState(ScreenState.BOOKS)) {
            label = Component.translatable("immersiveenchanting.tab.enchanting");
            icon = new ItemStack(Items.ENCHANTING_TABLE, 1);
        }

        graphics.renderItem(icon, screen.getGuiLeft() + 202, screen.getGuiTop() + 132);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Renders the hover tooltip and updates the cursor when the mouse is over the button.
     * <P>
     * <p>Displays the current label and changes the cursor to a pointing hand.</p>
     * <p>
     *
     * @param graphics the GUI graphics context
     * @param mouseX   the current mouse X position
     * @param mouseY   the current mouse Y position
     */
    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.isMouseOver(mouseX, mouseY)) {
            graphics.renderTooltip(Minecraft.getInstance().font, label, mouseX, mouseY);
        }

    }

    /**
     * Checks whether the mouse is currently hovering over the tab button.
     * <p>
     *
     * @param mouseX the current mouse X position
     * @param mouseY the current mouse Y position
     * @return {@code true} if the mouse is within the button bounds
     */
    private boolean isMouseOver(double mouseX, double mouseY) {
        return screen.isMouseOver(screen.getGuiLeft() + x, screen.getGuiTop() + y, width, height, mouseX, mouseY);
    }

    /**
     * Handles mouse click interactions for the tab button.
     * <P>
     * <p>If the button is clicked:</p>
     * <p>
     * <ul>
     *     <li>Plays a UI sound</li>
     *     <li>Switches between ENCHANTING and BOOKS screen states</li>
     *     <li>Initializes the book tab when entering BOOKS state</li>
     * </ul>
     * <p>
     *
     * @param mouse the mouse click event
     * @return {@code true} if the click was handled, otherwise falls back to default handling
     */
    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (this.isMouseOver(mouseX, mouseY)) {
            FxHelper.playGenericUISound(screen.player());

            if (screen.isState(ScreenState.ENCHANTING)) {
                screen.bookTab().init();
                screen.setState(ScreenState.BOOKS);
            } else if (screen.isState(ScreenState.BOOKS)) {
                screen.setState(ScreenState.ENCHANTING);
            }

            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouseX, mouseY, button);
    }
}
