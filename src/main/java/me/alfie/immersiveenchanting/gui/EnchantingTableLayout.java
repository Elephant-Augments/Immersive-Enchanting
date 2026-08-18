package me.alfie.immersiveenchanting.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * Shared layout for the enchanting table menu and screen.
 *
 * <p>Helps keep GUI elements aligned if slot coordinates or GUI dimensions change.</p>
 */
public final class EnchantingTableLayout {

    //Source texture size
    public static final int TEXTURE_SIZE = 256;

    //1.5× the 256 source texture
    public static final int GUI_WIDTH = 384;
    public static final int GUI_HEIGHT = 336;

    public static final int CANVAS_INSET = 4;
    //Y where the original texture’s inventory chrome begins
    public static final int TEXTURE_VIEWPORT_BOTTOM = 122;
    //Viewport hole plus its 4px bottom outline
    public static final int TEXTURE_VIEWPORT_FRAME_BOTTOM = 126;
    public static final int BOTTOM_CHROME_HEIGHT = TEXTURE_SIZE - TEXTURE_VIEWPORT_FRAME_BOTTOM;
    public static final int VIEWPORT_BOTTOM_IN_GUI = GUI_HEIGHT - (TEXTURE_SIZE - TEXTURE_VIEWPORT_BOTTOM);

    public static final int SCREEN_PADDING = 8;
    //GUI-space reserved on the right when JEI’s ingredient list is present
    public static final int JEI_RESERVED_WIDTH = 140;

    //Left inventory panel in the source texture
    public static final int TEXTURE_INVENTORY_U = 0;
    public static final int TEXTURE_INVENTORY_WIDTH = 196;
    //Right cost/gear strip in the source texture
    public static final int TEXTURE_COST_U = 224;
    public static final int TEXTURE_COST_WIDTH = 32;
    // Tab texture, including borders
    public static final int TEXTURE_TAB_U = 197;
    public static final int TEXTURE_TAB_V = 126;
    public static final int TEXTURE_TAB_WIDTH = 26;
    public static final int TEXTURE_TAB_HEIGHT = 28;
    // Item icon offset inside the tab sprite
    public static final int TAB_ICON_OFFSET_X = 5;
    public static final int TAB_ICON_OFFSET_Y = 6;
    // Color fill for the viewport's bottom border
    public static final int TEXTURE_TAB_FILL_U = TEXTURE_TAB_U + 8;
    public static final int TEXTURE_TAB_FILL_V = TEXTURE_TAB_V + 8;
    public static final int TEXTURE_FRAME_BLACK_U = 8;
    public static final int TEXTURE_FRAME_BLACK_V = 0;

    public static final int TAB_TO_INVENTORY_GAP = 0;
    public static final int INVENTORY_TO_COST_GAP = 4;

    public static final int BOTTOM_CLUSTER_WIDTH =
            TEXTURE_TAB_WIDTH + TAB_TO_INVENTORY_GAP
                    + TEXTURE_INVENTORY_WIDTH + INVENTORY_TO_COST_GAP
                    + TEXTURE_COST_WIDTH;
    public static final int BOTTOM_CLUSTER_X = (GUI_WIDTH - BOTTOM_CLUSTER_WIDTH) / 2;

    public static final int TAB_SPRITE_X = BOTTOM_CLUSTER_X;
    //Adjustments for the tab buttons to sit on the viewport’s bottom border
    public static final int TAB_SPRITE_Y = VIEWPORT_BOTTOM_IN_GUI - 1;
    public static final int TAB_BUTTON_X = TAB_SPRITE_X + TAB_ICON_OFFSET_X;
    public static final int TAB_BUTTON_Y = TAB_SPRITE_Y + TAB_ICON_OFFSET_Y;
    public static final int TAB_BUTTON_WIDTH = 20;
    public static final int TAB_BUTTON_HEIGHT = 25;

    public static final int INVENTORY_PANEL_X = TAB_SPRITE_X + TEXTURE_TAB_WIDTH + TAB_TO_INVENTORY_GAP;
    public static final int COST_PANEL_X = INVENTORY_PANEL_X + TEXTURE_INVENTORY_WIDTH + INVENTORY_TO_COST_GAP;

    public static final int PLAYER_INVENTORY_GRID_X = INVENTORY_PANEL_X + 17;
    public static final int PLAYER_INVENTORY_GRID_Y =
            VIEWPORT_BOTTOM_IN_GUI + (140 - TEXTURE_VIEWPORT_FRAME_BOTTOM);
    public static final int PLAYER_INVENTORY_SLOT_SIZE = 18;
    public static final int INVENTORY_TO_HOTBAR_GAP = 4;
    public static final int PLAYER_HOTBAR_GRID_Y =
            PLAYER_INVENTORY_GRID_Y + PLAYER_INVENTORY_SLOT_SIZE * 3 + INVENTORY_TO_HOTBAR_GAP;

    public static final int INVENTORY_LABEL_OFFSET_Y = -11;
    public static final int INVENTORY_LABEL_OFFSET_X = 0;

    public static final int PLAYER_MAIN_INVENTORY_START_INDEX = 9;

    public static final int COST_COLUMN_X = COST_PANEL_X + (233 - TEXTURE_COST_U);
    public static final int TOOL_SLOT_Y = PLAYER_INVENTORY_GRID_Y + (141 - 140);
    public static final int COST_SLOT_Y = PLAYER_INVENTORY_GRID_Y + (170 - 140);
    public static final int FUEL_SLOT_Y = PLAYER_INVENTORY_GRID_Y + (199 - 140);

    private EnchantingTableLayout() {}

    public record CanvasViewport(int x, int y, int width, int height) {}

    /** Actual viewport area within the GUI. */
    public static CanvasViewport canvasViewport(int guiLeft, int guiTop) {
        return new CanvasViewport(
                guiLeft + CANVAS_INSET,
                guiTop + CANVAS_INSET,
                GUI_WIDTH - CANVAS_INSET * 2,
                VIEWPORT_BOTTOM_IN_GUI - CANVAS_INSET * 2
        );
    }

    public static int guiTop(int screenHeight) {
        return Math.max(0, (screenHeight - GUI_HEIGHT) / 2);
    }

    /**
     * Shifts the GUI the left when JEI is present, otherwise centers it.
     */
    public static int guiLeft(int screenWidth, boolean jeiLoaded) {
        int centered = (screenWidth - GUI_WIDTH) / 2;
        if (!jeiLoaded) {
            return centered;
        }
        int maxLeft = screenWidth - GUI_WIDTH - JEI_RESERVED_WIDTH;
        if (maxLeft < SCREEN_PADDING) {
            maxLeft = SCREEN_PADDING;
        }
        return Math.min(centered, maxLeft);
    }

    public static boolean isJeiLoaded() {
        return ModList.get().isLoaded("jei");
    }

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
