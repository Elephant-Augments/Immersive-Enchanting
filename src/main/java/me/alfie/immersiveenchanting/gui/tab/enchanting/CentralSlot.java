package me.alfie.immersiveenchanting.gui.tab.enchanting;

import com.mojang.blaze3d.platform.InputConstants;
import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.ScreenEventListener;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.alfinolib.gui.util.MousePos;
import me.alfie.alfinolib.networking.Networking;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.networking.UpdateToolSlotPacket;
import net.minecraft.world.item.ItemStack;

public class CentralSlot extends CanvasRenderable implements ScreenEventListener {

    public CentralSlot(Canvas canvas) {
        super(canvas);
    }

     @Override
    public void render(GuiGraphicsX gx, MousePos mousePos) {
        setCenterPos(Sprite.ENCHANTING_TABLE_TOP);
        blit(gx, Sprite.ENCHANTING_TABLE_TOP, Canvas.FULL_BRIGHTNESS);

        ItemStack toolSlotStack = canvas().screen().getMenu().getToolSlot().getItem();

        if(!toolSlotStack.isEmpty()) {
            blit(gx, Sprite.BOOK_OPEN, Canvas.FULL_BRIGHTNESS);
            renderItem(gx, toolSlotStack, mousePos);
        } else {
            blit(gx, Sprite.BOOK_CLOSED, Canvas.FULL_BRIGHTNESS);
        }
    }

    private void renderItem(GuiGraphicsX gx, ItemStack stack, MousePos mousePos) {
        setCenterPos(16, 16);
        GuiGraphicsApi.itemStack(gx, stack, canvas().screen().getFont(), (int) canvasX(), (int) canvasY());
        setCenterPos(Sprite.ENCHANTING_TABLE_TOP);

        // Defer tooltip to end-of-frame so other tooltip mods get a clean screen-space pass.
        if(canvas().isMouseOver(canvasX() + 8, canvasY() + 8, 16, 16, mousePos)) {
            canvas().screen().requestCentralItemTooltip(stack);
        }
    }


    @Override
    public boolean onMouseClick(MousePos mousePos, int button) {
        if(button != InputConstants.MOUSE_BUTTON_LEFT) return ScreenEventListener.super.onMouseClick(mousePos, button);

        if(canvas().isMouseOver(canvasX(), canvasY(), 32, 32, mousePos)) {
            UpdateToolSlotPacket.Mode mode = canvas().screen().getMenu().getCarried().isEmpty() ?
                    UpdateToolSlotPacket.Mode.TAKE : UpdateToolSlotPacket.Mode.PLACE;

            Networking.sendToServer(new UpdateToolSlotPacket(mode.ordinal()));
            return true;
        }

        return ScreenEventListener.super.onMouseClick(mousePos, button);
    }
}
