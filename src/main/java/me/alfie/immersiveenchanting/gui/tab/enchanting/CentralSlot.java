package me.alfie.immersiveenchanting.gui.tab.enchanting;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.networking.UpdateToolSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CentralSlot extends CanvasRenderable implements ScreenEventListener {

    public CentralSlot(Canvas canvas) {
        super(canvas);
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        setCenterPos(Sprite.ENCHANTING_TABLE_TOP);
        blit(graphics, Sprite.ENCHANTING_TABLE_TOP, Canvas.FULL_BRIGHTNESS);

        ItemStack toolSlotStack = canvas().screen().getMenu().getToolSlot().getItem();

        if(!toolSlotStack.isEmpty()) {
            blit(graphics, Sprite.BOOK_OPEN, Canvas.FULL_BRIGHTNESS);
            renderItem(graphics, toolSlotStack, mouseX, mouseY);
        } else {
            blit(graphics, Sprite.BOOK_CLOSED, Canvas.FULL_BRIGHTNESS);
        }

        if(canvas().isMouseOver(canvasX(), canvasY(), 32, 32, mouseX, mouseY)) {
            if(!canvas().screen().getMenu().getCarried().isEmpty() || canvas().screen().getMenu().getToolSlot().hasItem()) {
                graphics.requestCursor(CursorTypes.POINTING_HAND);
            }
        }
    }

    private void renderItem(GuiGraphicsExtractor graphics, ItemStack stack, int mouseX, int mouseY) {
        setCenterPos(16, 16);
        graphics.item(stack, (int) canvasX(), (int) canvasY());

        if(canvas().isMouseOver(canvasX(), canvasY(), 16, 16, mouseX, mouseY) && !canvas().screen().tooltipManager().isTooltipLocked()) {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, stack, mouseX, mouseY);
        }

        setCenterPos(Sprite.ENCHANTING_TABLE_TOP);
    }

    @Override
    public boolean onMouseClick(MouseButtonEvent mouse) {
        if(mouse.button() != InputConstants.MOUSE_BUTTON_LEFT) return ScreenEventListener.super.onMouseClick(mouse);

        if(canvas().isMouseOver(canvasX(), canvasY(), 32, 32, mouse.x(), mouse.y())) {

           UpdateToolSlotPacket.Mode mode = canvas().screen().getMenu().getCarried().isEmpty() ?
                    UpdateToolSlotPacket.Mode.TAKE : UpdateToolSlotPacket.Mode.PLACE;

            ClientPacketDistributor.sendToServer(new UpdateToolSlotPacket(mode.ordinal()));

            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouse);
    }
}
