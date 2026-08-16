package me.alfie.immersiveenchanting.gui;

import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

/**
 * Shared layout constants for the enchanting table menu and screen.
 *
 * <p>Helps keep GUI elements aligned if slot coordinates or GUI dimensions change.</p>
 */
public final class EnchantingTableLayout {

    public static final int GUI_WIDTH = Sprite.ENCHANTING_TABLE_GUI.width();
    public static final int GUI_HEIGHT = Sprite.ENCHANTING_TABLE_GUI.height();

    public static final int PLAYER_INVENTORY_GRID_X = 17;
    public static final int PLAYER_INVENTORY_GRID_Y = 140;
    public static final int PLAYER_INVENTORY_SLOT_SIZE = 18;
    public static final int INVENTORY_TO_HOTBAR_GAP = 4;
    public static final int PLAYER_HOTBAR_GRID_Y =
            PLAYER_INVENTORY_GRID_Y + PLAYER_INVENTORY_SLOT_SIZE * 3 + INVENTORY_TO_HOTBAR_GAP;

    public static final int INVENTORY_LABEL_OFFSET_Y = -11;
    public static final int INVENTORY_LABEL_OFFSET_X = 0;

    /** First {@link Inventory} slot index for the main (non-hotbar) grid. */
    public static final int PLAYER_MAIN_INVENTORY_START_INDEX = 9;

    private EnchantingTableLayout() {}

    public static int inventoryLabelX(EnchantingTableMenu menu) {
        Slot anchor = getPlayerMainInventoryAnchorSlot(menu);
        return anchor != null
                ? anchor.x + INVENTORY_LABEL_OFFSET_X
                : PLAYER_INVENTORY_GRID_X + INVENTORY_LABEL_OFFSET_X;
    }

    public static int inventoryLabelY(EnchantingTableMenu menu) {
        Slot anchor = getPlayerMainInventoryAnchorSlot(menu);
        return anchor != null
                ? anchor.y + INVENTORY_LABEL_OFFSET_Y
                : PLAYER_INVENTORY_GRID_Y + INVENTORY_LABEL_OFFSET_Y;
    }

    @Nullable
    public static Slot getPlayerMainInventoryAnchorSlot(EnchantingTableMenu menu) {
        for (Slot slot : menu.slots) {
            if (slot.container instanceof Inventory && slot.getContainerSlot() == PLAYER_MAIN_INVENTORY_START_INDEX) {
                return slot;
            }
        }
        return null;
    }
}
