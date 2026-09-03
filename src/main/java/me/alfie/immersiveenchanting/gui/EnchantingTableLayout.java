package me.alfie.immersiveenchanting.gui;

import me.alfie.alfinolib.gui.GuiGraphicsX;
import me.alfie.alfinolib.gui.util.GuiGraphicsApi;
import me.alfie.immersiveenchanting.gui.core.Sprite;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
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
    /** Bottom viewport outline is one pixel shorter than the top/sides. */
    public static final int VIEWPORT_BOTTOM_BORDER = CANVAS_INSET - 1;
    //Y where the original texture’s inventory chrome begins
    public static final int TEXTURE_VIEWPORT_BOTTOM = 122;
    //Viewport hole plus its 4px bottom outline
    public static final int TEXTURE_VIEWPORT_FRAME_BOTTOM = TEXTURE_VIEWPORT_BOTTOM + CANVAS_INSET;
    public static final int BOTTOM_CHROME_HEIGHT = TEXTURE_SIZE - TEXTURE_VIEWPORT_FRAME_BOTTOM;
    public static final int VIEWPORT_BOTTOM_IN_GUI = GUI_HEIGHT - TEXTURE_SIZE + TEXTURE_VIEWPORT_BOTTOM;

    public static final int SCREEN_PADDING = 8;
    //GUI-space reserved on the right when JEI’s ingredient list is present
    public static final int JEI_RESERVED_WIDTH = 140;

    public static final int TEXTURE_INVENTORY_U = 0;
    public static final int TEXTURE_INVENTORY_WIDTH = 196;
    public static final int TEXTURE_COST_U = 224;
    public static final int TEXTURE_COST_WIDTH = 32;
    public static final int TEXTURE_TAB_U = 197;
    public static final int TEXTURE_TAB_V = TEXTURE_VIEWPORT_FRAME_BOTTOM;
    public static final int TEXTURE_TAB_WIDTH = 26;
    public static final int TEXTURE_TAB_HEIGHT = 28;
    public static final int TAB_ICON_OFFSET_X = 5;
    public static final int TAB_ICON_OFFSET_Y = 6;
    public static final int TEXTURE_TAB_FILL_U = TEXTURE_TAB_U + 8;
    public static final int TEXTURE_TAB_FILL_V = TEXTURE_TAB_V + 8;
    public static final int TEXTURE_FRAME_BLACK_U = 8;
    public static final int TEXTURE_FRAME_BLACK_V = 0;

    public static final int MOD_FILTER_HELP_INSET = CANVAS_INSET + 2;
    public static final int MOD_FILTER_HELP_SIZE = 10;

    public static final int SEARCH_FIELD_WIDTH = 100;
    public static final int SEARCH_FIELD_HEIGHT = 16;
    public static final int SEARCH_FIELD_TEXT_PADDING = 3;

    public static final int ENCHANTING_SEARCH_WIDTH = 96;
    public static final int ENCHANTING_SEARCH_HEIGHT = SEARCH_FIELD_HEIGHT;
    public static final int ENCHANTING_SEARCH_X = GUI_WIDTH - MOD_FILTER_HELP_INSET - ENCHANTING_SEARCH_WIDTH;
    public static final int ENCHANTING_SEARCH_Y = CANVAS_INSET + 2;

    public static final int TAB_TO_INVENTORY_GAP = 2;
    public static final int INVENTORY_TO_COST_GAP = 4;

    public static final int BOTTOM_CLUSTER_WIDTH =
            TEXTURE_TAB_WIDTH + TAB_TO_INVENTORY_GAP
                    + TEXTURE_INVENTORY_WIDTH + INVENTORY_TO_COST_GAP
                    + TEXTURE_COST_WIDTH;
    public static final int BOTTOM_CLUSTER_X = (GUI_WIDTH - BOTTOM_CLUSTER_WIDTH) / 2;

    public static final int TAB_SPRITE_X = BOTTOM_CLUSTER_X;
    public static final int TAB_SPRITE_Y = VIEWPORT_BOTTOM_IN_GUI - 1;
    public static final int TAB_BUTTON_X = TAB_SPRITE_X + TAB_ICON_OFFSET_X;
    public static final int TAB_BUTTON_Y = TAB_SPRITE_Y + TAB_ICON_OFFSET_Y;
    public static final int TAB_BUTTON_WIDTH = 20;
    public static final int TAB_BUTTON_HEIGHT = 25;

    public static final int INVENTORY_PANEL_X = TAB_SPRITE_X + TEXTURE_TAB_WIDTH + TAB_TO_INVENTORY_GAP;
    public static final int COST_PANEL_X = INVENTORY_PANEL_X + TEXTURE_INVENTORY_WIDTH + INVENTORY_TO_COST_GAP;

    //Original texture Y of the player inventory grid
    public static final int TEXTURE_INVENTORY_GRID_Y = 140;
    public static final int PLAYER_INVENTORY_GRID_X = INVENTORY_PANEL_X + 17;
    public static final int PLAYER_INVENTORY_GRID_Y =
            VIEWPORT_BOTTOM_IN_GUI + TEXTURE_INVENTORY_GRID_Y - TEXTURE_VIEWPORT_FRAME_BOTTOM;
    public static final int PLAYER_INVENTORY_SLOT_SIZE = 18;
    public static final int INVENTORY_TO_HOTBAR_GAP = 4;
    public static final int PLAYER_HOTBAR_GRID_Y =
            PLAYER_INVENTORY_GRID_Y + PLAYER_INVENTORY_SLOT_SIZE * 3 + INVENTORY_TO_HOTBAR_GAP;

    public static final int INVENTORY_LABEL_OFFSET_Y = -11;
    public static final int PLAYER_MAIN_INVENTORY_START_INDEX = 9;

    public static final int COST_COLUMN_X = COST_PANEL_X + 9;
    public static final int TOOL_SLOT_Y = PLAYER_INVENTORY_GRID_Y + 1;
    public static final int COST_SLOT_Y = PLAYER_INVENTORY_GRID_Y + 30;
    public static final int FUEL_SLOT_Y = PLAYER_INVENTORY_GRID_Y + 59;

    public static final float BOOK_LIST_WIDTH_SCALE = 1.4f;
    public static final int BOOK_BOX_SOURCE_WIDTH = 108;
    public static final int BOOK_BOX_SOURCE_HEIGHT = 19;
    public static final int BOOK_LIST_WIDTH = Math.round(BOOK_BOX_SOURCE_WIDTH * BOOK_LIST_WIDTH_SCALE);
    public static final int BOOK_BOX_HEIGHT = BOOK_BOX_SOURCE_HEIGHT;
    public static final int BOOK_VISIBLE_ROWS = 7;
    public static final int BOOK_SCROLLBAR_GAP = 4;
    public static final int BOOK_SCROLLBAR_WIDTH = 14;
    public static final int BOOK_CONTENT_WIDTH = BOOK_LIST_WIDTH + BOOK_SCROLLBAR_GAP + BOOK_SCROLLBAR_WIDTH;
    public static final int BOOK_LIST_X = (GUI_WIDTH - BOOK_CONTENT_WIDTH) / 2;
    public static final int BOOK_SCROLLBAR_X = BOOK_LIST_X + BOOK_LIST_WIDTH + BOOK_SCROLLBAR_GAP;
    public static final int BOOK_SEARCH_Y = 6;
    public static final int BOOK_SEARCH_WIDTH = BOOK_LIST_WIDTH;
    public static final int BOOK_SEARCH_HEIGHT = SEARCH_FIELD_HEIGHT;
    public static final int BOOK_TOTAL_LABEL_Y = BOOK_SEARCH_Y + BOOK_SEARCH_HEIGHT + 4;
    public static final int BOOK_LIST_Y = BOOK_TOTAL_LABEL_Y + 12;
    public static final int BOOK_SCROLLBAR_HEIGHT = BOOK_BOX_HEIGHT * BOOK_VISIBLE_ROWS;
    public static final int BOOK_TITLE_PADDING = 4;
    /** Minimum scale applied when fitting long titles into the row. */
    public static final float BOOK_TITLE_MIN_SCALE = 0.7f;

    private EnchantingTableLayout() {}

    public static void renderSearchField(GuiGraphicsX gx, Font font, int x, int y, String text) {
        renderSearchField(gx, font, x, y, text, SEARCH_FIELD_WIDTH);
    }

    public static void renderSearchField(
            GuiGraphicsX gx,
            Font font,
            int x,
            int y,
            String text,
            int width
    ) {
        GuiGraphicsApi.blit(
                gx,
                Sprite.SEARCH.id(),
                x, y,
                width, SEARCH_FIELD_HEIGHT
        );

        GuiGraphicsApi.text(
                gx,
                font,
                Component.literal(text),
                x + SEARCH_FIELD_TEXT_PADDING,
                y + SEARCH_FIELD_TEXT_PADDING + 1,
                true
        );
    }

    public static boolean searchFieldContains(int mouseX, int mouseY, int x, int y) {
        return searchFieldContains(mouseX, mouseY, x, y, SEARCH_FIELD_WIDTH);
    }

    public static boolean searchFieldContains(int mouseX, int mouseY, int x, int y, int width) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + SEARCH_FIELD_HEIGHT;
    }

    public record CanvasViewport(int x, int y, int width, int height) {}

    /** Actual viewport area within the GUI. */
    public static CanvasViewport canvasViewport(int guiLeft, int guiTop) {
        return new CanvasViewport(
                guiLeft + CANVAS_INSET,
                guiTop + CANVAS_INSET,
                GUI_WIDTH - CANVAS_INSET * 2,
                VIEWPORT_BOTTOM_IN_GUI - CANVAS_INSET - VIEWPORT_BOTTOM_BORDER
        );
    }

    public static int guiTop(int screenHeight) {
        return Math.max(0, (screenHeight - GUI_HEIGHT) / 2);
    }

    /**
     * Shifts the GUI left when JEI is present, otherwise centers it.
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
        return anchor != null ? anchor.x : PLAYER_INVENTORY_GRID_X;
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
