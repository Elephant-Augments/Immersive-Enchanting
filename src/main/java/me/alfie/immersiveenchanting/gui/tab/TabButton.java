package me.alfie.immersiveenchanting.gui.tab;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.ScreenEventListener;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.immersiveenchanting.gui.EnchantingTableLayout;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class TabButton implements ScreenEventListener {

    public final EnchantingTableScreen screen;

    private final int width = EnchantingTableLayout.TAB_BUTTON_WIDTH;
    private final int height = EnchantingTableLayout.TAB_BUTTON_HEIGHT;

    private ItemStack icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
    private Component label = Component.empty();

    /**
     * Creates a new tab button tied to the given screen.
     <P>
     * @param screen the parent {@link EnchantingTableScreen} this button belongs to
     */
    public TabButton(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    /**
     * Renders the tab button, including its icon and hover tooltip.
     <P>
     * <p>The displayed icon and label depend on the current {@link ScreenState}:</p>
     <P>
     * <ul>
     *     <li>ENCHANTING → shows book icon (switch to books view)</li>
     *     <li>BOOKS → shows enchanting table icon (switch to enchanting view)</li>
     * </ul>
     <P>

     */
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        if(screen.isState(ScreenState.ENCHANTING)) {
            label = Component.translatable("immersiveenchanting.tab.book");
            icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
        } else if (screen.isState(ScreenState.BOOKS)) {
            label = Component.translatable("immersiveenchanting.tab.enchanting");
            icon = new ItemStack(Items.ENCHANTING_TABLE, 1);
        }

        final int x = screen.getGuiLeft() + EnchantingTableLayout.TAB_BUTTON_X;
        final int y = screen.getGuiTop() + EnchantingTableLayout.TAB_BUTTON_Y;
        GuiGraphicsApi.itemStack(gx, icon, screen.getFont(), x, y);
        if(mousePos.isOver(x, y, width, height)) {
            screen.requestTabTooltip(label);
        }
    }

    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(mousePos.isOver(screen.getGuiLeft() + EnchantingTableLayout.TAB_BUTTON_X, screen.getGuiTop() + EnchantingTableLayout.TAB_BUTTON_Y, width, height)) {
            FxHelper.playGenericUISound(screen.player());

            if(screen.isState(ScreenState.ENCHANTING)) {
                screen.bookTab().init();
                screen.setState(ScreenState.BOOKS);
            } else if(screen.isState(ScreenState.BOOKS)) {
                screen.setState(ScreenState.ENCHANTING);
            }

            return true;
        }

        return ScreenEventListener.super.onMouseClick(mousePos, button);
    }
}
