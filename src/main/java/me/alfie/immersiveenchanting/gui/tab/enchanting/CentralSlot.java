package me.alfie.immersiveenchanting.gui.tab.enchanting;

import me.alfie.immersiveenchanting.gui.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.ScrollableCanvas;
import me.alfie.immersiveenchanting.gui.Sprite;
import me.alfie.immersiveenchanting.networking.UpdateToolSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CentralSlot extends CanvasRenderable implements ScreenEventListener {

    public CentralSlot(ScrollableCanvas canvas) {
        super(canvas);
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        setX(canvas().getLocalCenterPos(
                        Sprite.ENCHANTING_TABLE_TOP.width(),
                        Sprite.ENCHANTING_TABLE_TOP.height()).x());
        setY(canvas().getLocalCenterPos(
                        Sprite.ENCHANTING_TABLE_TOP.width(),
                        Sprite.ENCHANTING_TABLE_TOP.height()).y());

        blit(Sprite.ENCHANTING_TABLE_TOP, graphics);
        Sprite bookSprite = canvas().screen().getMenu().getToolSlot().getItem().isEmpty() ?
                Sprite.BOOK_CLOSED : Sprite.BOOK_OPEN;
        blit(bookSprite, graphics);

        setX(canvas().getLocalCenterPos(16, 16).x());
        setY(canvas().getLocalCenterPos(16, 16).y());
        ItemStack stack = canvas().screen().getMenu().getToolSlot().getItem();
        graphics.item(stack, getScreenX(), getScreenY());

        if(canvas().isMouseOver(getScreenX(), getScreenY(), 16, 16, mouseX, mouseY)
        && !canvas().screen().getMenu().getToolSlot().getItem().isEmpty())
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, stack, mouseX, mouseY);
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent mouse) {
        if(canvas().isMouseOver(getScreenX(), getScreenY(),
                16, 16,
                mouse.x(), mouse.y())) {

            UpdateToolSlotPacket.Mode mode = canvas().screen().getMenu().getToolSlot().getItem().isEmpty() ?
                    UpdateToolSlotPacket.Mode.PLACE : UpdateToolSlotPacket.Mode.TAKE;

            ClientPacketDistributor.sendToServer(new UpdateToolSlotPacket(mode.ordinal()));
            return true;
        }
        return false;
    }
}
