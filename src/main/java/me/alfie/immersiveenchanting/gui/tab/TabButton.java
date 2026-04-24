package me.alfie.immersiveenchanting.gui.tab;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.ScreenState;
import me.alfie.immersiveenchanting.item.ModItems;
import me.alfie.immersiveenchanting.util.FxHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
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

    public TabButton(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if(screen.isState(ScreenState.ENCHANTING)) {
            label = Component.translatable("immersiveenchanting.tab.book");
            icon = new ItemStack(ModItems.ANCIENT_BOOK.get(), 1);
        } else if (screen.isState(ScreenState.BOOKS)) {
            label = Component.translatable("immersiveenchanting.tab.enchanting");
            icon = new ItemStack(Items.ENCHANTING_TABLE, 1);
        }

        graphics.item(icon, screen.getGuiLeft()+202, screen.getGuiTop()+132);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }

    private void renderHoverTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if(this.isMouseOver(mouseX, mouseY)) {
            graphics.requestCursor(CursorTypes.POINTING_HAND);
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, label, mouseX, mouseY);
        }

    }

    private boolean isMouseOver(double mouseX, double mouseY) {
        return screen.isMouseOver(screen.getGuiLeft() + x, screen.getGuiTop() + y, width, height, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent mouse) {
        if(this.isMouseOver(mouse.x(), mouse.y())) {
            FxHelper.playGenericUISound(screen.player());

            if(screen.isState(ScreenState.ENCHANTING)) {
                screen.bookTab().init();
                screen.setState(ScreenState.BOOKS);
            } else if(screen.isState(ScreenState.BOOKS)) {
                screen.setState(ScreenState.ENCHANTING);
            }

            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouse);
    }
}
