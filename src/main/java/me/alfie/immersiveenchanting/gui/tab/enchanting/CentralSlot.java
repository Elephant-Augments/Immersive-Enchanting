package me.alfie.immersiveenchanting.gui.tab.enchanting;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.alfie.immersiveenchanting.gui.canvas.Canvas;
import me.alfie.immersiveenchanting.gui.canvas.CanvasRenderable;
import me.alfie.immersiveenchanting.gui.core.ScreenEventListener;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.networking.UpdateToolSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class CentralSlot extends CanvasRenderable implements ScreenEventListener {

    public CentralSlot(Canvas canvas) {
        super(canvas);
    }

    /**
     * Renders the central enchanting slot, including:
     * - Background sprite
     * - Book open/closed state
     * - Tool item (if present)
     * <p>
     * Also handles hover cursor changes.
     *
     * @param graphics Rendering context
     * @param mouseX   Mouse X position
     * @param mouseY   Mouse Y position
     */
    public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        setCenterPos(Sprite.ENCHANTING_TABLE_TOP);
        blit(graphics, Sprite.ENCHANTING_TABLE_TOP, Canvas.FULL_BRIGHTNESS);

        ItemStack toolSlotStack = canvas().screen().getMenu().getToolSlot().getItem();

        if (!toolSlotStack.isEmpty()) {
            blit(graphics, Sprite.BOOK_OPEN, Canvas.FULL_BRIGHTNESS);
            renderItem(graphics, toolSlotStack, mouseX, mouseY);
        } else {
            blit(graphics, Sprite.BOOK_CLOSED, Canvas.FULL_BRIGHTNESS);
        }
    }

    /**
     * Renders the item inside the central slot and displays its tooltip when hovered.
     *
     * @param graphics Rendering context
     * @param stack    Item stack to render
     * @param mouseX   Mouse X position
     * @param mouseY   Mouse Y position
     */
    private void renderItem(GuiGraphics graphics, ItemStack stack, int mouseX, int mouseY) {
        setCenterPos(16, 16);
        graphics.renderItem(stack, (int) canvasX(), (int) canvasY());

        graphics.pose().pushPose();
        graphics.pose().setIdentity();
        RenderSystem.disableScissor();
        if (canvas().isMouseOver(canvasX(), canvasY(), 16, 16, mouseX, mouseY) && !canvas().screen().tooltipManager().isTooltipLocked()) {
            graphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
        }
        graphics.pose().popPose();

        setCenterPos(Sprite.ENCHANTING_TABLE_TOP);
    }

    /**
     * Handles mouse click interactions on the central slot.
     *
     * <p>Left-click behavior:
     * <ul>
     *     <li>If cursor is empty → sends TAKE packet</li>
     *     <li>If cursor has item → sends PLACE packet</li>
     * </ul>
     *
     * @param mouse Mouse event
     * @return true if the click was handled
     */
    @Override
    public boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT)
            return ScreenEventListener.super.onMouseClick(mouseX, mouseY, button);

        if (canvas().isMouseOver(canvasX(), canvasY(), 32, 32, mouseX, mouseY)) {

            UpdateToolSlotPacket.Mode mode = canvas().screen().getMenu().getCarried().isEmpty() ?
                    UpdateToolSlotPacket.Mode.TAKE : UpdateToolSlotPacket.Mode.PLACE;

            PacketDistributor.sendToServer(new UpdateToolSlotPacket(mode.ordinal()));

            return true;
        }

        return ScreenEventListener.super.onMouseClick(mouseX, mouseY, button);
    }
}
