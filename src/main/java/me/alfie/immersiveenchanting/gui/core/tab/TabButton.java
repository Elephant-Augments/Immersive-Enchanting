package me.alfie.immersiveenchanting.gui.core.tab;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TabButton {

    public final EnchantingTableScreen screen;

    private final int width = 20;
    private final int height = 25;
    private final int x = 200;
    private final int y = 126;

    private ItemStack tabIcon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
    private String translationKey = "gui.immersiveenchanting.switch_book_tab";

    public TabButton(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {

        //Set state
        if(screen.getState().equals(ScreenState.ENCHANTING)) {
            translationKey = "gui.immersiveenchanting.switch_book_tab";
            tabIcon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
        } else if (screen.getState().equals(ScreenState.BOOKS)) {
            translationKey = "gui.immersiveenchanting.switch_enchanting_tab";
            tabIcon = new ItemStack(Items.ENCHANTING_TABLE, 1);
        }

        //Draw tab icon
        guiGraphics.renderItem(tabIcon, screen.getGuiLeft()+202, screen.getGuiTop()+132);
        renderHoverTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(isMouseOver(mouseX, mouseY)) {
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.translatable(translationKey),
                    mouseX, mouseY
            );
        }
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return screen.isMouseOver(mouseX, mouseY,
                screen.getGuiLeft() + x,
                screen.getGuiTop() + y,
                width,
                height);
    }

    public boolean onMouseClick(int mouseX, int mouseY) {
        if(isMouseOver(mouseX, mouseY)) {
            FxHelper.playGenericUISound(screen.player);

            if(screen.getState().equals(ScreenState.ENCHANTING)) {
                screen.setState(ScreenState.BOOKS);
            } else if(screen.getState().equals(ScreenState.BOOKS)) {
                screen.setState(ScreenState.ENCHANTING);
            }
            return true;
        }
        return false;
    }
}
