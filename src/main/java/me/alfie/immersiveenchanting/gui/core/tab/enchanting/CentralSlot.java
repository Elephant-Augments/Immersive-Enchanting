package me.alfie.immersiveenchanting.gui.core.tab.enchanting;

import me.alfie.immersiveenchanting.gui.EnchantingTableScreen;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import me.alfie.immersiveenchanting.networking.packet.updatetoolslot.UpdateToolSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector2i;

public class CentralSlot {

    public final EnchantingTableScreen screen;
    private Vector2i pos;
    private final int size = 16;

    public CentralSlot(EnchantingTableScreen screen) {
        this.screen = screen;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = screen.getCanvas().getCenterPos(32, 32).x - (int) screen.getCanvas().getScrollX();
        int y = screen.getCanvas().getCenterPos(32, 32).y - (int) screen.getCanvas().getScrollY();

        guiGraphics.blit(
                Sprite.ENCHANTING_TABLE_TOP.get(),
                x,
                y,
                0f, 0f, 32, 32,
                32, 32
        );
        pos = screen.getCanvas().getCenterPos(size, size);

        if (screen.getMenu().getToolSlotItem().isEmpty()) guiGraphics.blit(
                    Sprite.BOOK_CLOSED.get(),
                    x,
                    y,
                    0f, 0f, 32, 32,
                    32, 32);
        else guiGraphics.blit(
                    Sprite.BOOK_OPEN.get(),
                    x,
                    y,
                    0f, 0f, 32, 32,
                    32, 32);

        //Render the item
        ItemStack stack = screen.getMenu().getToolSlotItem();
        guiGraphics.renderItem(stack, screen.getCanvas().getCenterPos(16, 16).x - (int) screen.getCanvas().getScrollX(),
                screen.getCanvas().getCenterPos(16, 16).y - (int) screen.getCanvas().getScrollY());
    }

    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if(!screen.getMenu().getToolSlotItem().is(Items.AIR) && !screen.enchantingTab.isLockHover()) {
            if(screen.getCanvas().isMouseOverBoundingBox(pos, size, size, mouseX, mouseY)) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 500);
                guiGraphics.renderTooltip(Minecraft.getInstance().font,
                        screen.getMenu().getToolSlotItem(),
                        mouseX, mouseY);
                guiGraphics.pose().popPose();
            }
        }
    }

    public boolean onMouseClick(int mouseX, int mouseY) {
        if (screen.getCanvas().isMouseOverBoundingBox(pos, size, size, mouseX, mouseY)) {

            int mode;
            if(screen.getMenu().getCarried().isEmpty() && !screen.enchantingTab.isLockHover()) {
                mode = UpdateToolSlotPacket.MODE.TAKE.ordinal();
            } else {
                mode = UpdateToolSlotPacket.MODE.PLACE.ordinal();
            }

            PacketDistributor.sendToServer(new UpdateToolSlotPacket(mode));
            return true;
        }
        return false;
    }

}
